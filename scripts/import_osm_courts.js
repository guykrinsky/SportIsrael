/**
 * Imports real sports courts in Israel from OpenStreetMap (Overpass API)
 * into the Firestore "courts" collection used by the app's map.
 *
 * OSM data is licensed ODbL: storing it is allowed with attribution,
 * which this script writes into every court's description.
 *
 * Usage:
 *   cd scripts
 *   npm install
 *   node import_osm_courts.js                 # dry run (prints, writes nothing)
 *   node import_osm_courts.js --write         # actually import
 *   node import_osm_courts.js --write --sports=basketball --limit=100
 *
 * Options:
 *   --sports=a,b   comma list of: basketball,soccer,tennis,volleyball  (default: basketball,soccer)
 *   --limit=N      max courts imported PER SPORT                        (default: 150)
 *   --write        perform Firestore writes (omit for dry run)
 *
 * Requires scripts/serviceAccountKey.json:
 *   Firebase console -> Project settings -> Service accounts -> Generate new private key.
 *   That file is secret - it is gitignored, never commit it.
 */

const fs = require('fs');
const path = require('path');

const OVERPASS_URL = 'https://overpass-api.de/api/interpreter';
const ATTRIBUTION = 'Data © OpenStreetMap contributors (ODbL)';
const DEFAULT_SPORTS = ['basketball', 'soccer'];
const DEFAULT_LIMIT = 150;

function parseArgs() {
  const args = { sports: DEFAULT_SPORTS, limit: DEFAULT_LIMIT, write: false };
  for (const arg of process.argv.slice(2)) {
    if (arg === '--write') args.write = true;
    else if (arg.startsWith('--sports=')) {
      args.sports = arg.substring('--sports='.length).split(',').map(s => s.trim().toLowerCase()).filter(Boolean);
    } else if (arg.startsWith('--limit=')) {
      args.limit = parseInt(arg.substring('--limit='.length), 10);
      if (!Number.isFinite(args.limit) || args.limit < 1) {
        console.error('Invalid --limit'); process.exit(1);
      }
    } else {
      console.error(`Unknown argument: ${arg}`); process.exit(1);
    }
  }
  return args;
}

async function fetchOsmCourts(sports) {
  // nwr = nodes + ways + relations; "out center" gives a single lat/lon for areas.
  const sportRegex = sports.join('|');
  const query = `
    [out:json][timeout:180];
    area["ISO3166-1"="IL"][admin_level=2]->.il;
    nwr["leisure"="pitch"]["sport"~"^(${sportRegex})$"](area.il);
    out center tags;
  `;
  console.log(`Querying Overpass for: ${sports.join(', ')} pitches in Israel...`);
  const response = await fetch(OVERPASS_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'User-Agent': 'SportIsrael-court-import/1.0 (one-time import script)',
      'Accept': 'application/json',
    },
    body: 'data=' + encodeURIComponent(query),
  });
  if (!response.ok) {
    throw new Error(`Overpass returned HTTP ${response.status}: ${await response.text()}`);
  }
  const json = await response.json();
  return json.elements || [];
}

function toCourt(element) {
  const tags = element.tags || {};
  const lat = element.lat !== undefined ? element.lat : (element.center && element.center.lat);
  const lon = element.lon !== undefined ? element.lon : (element.center && element.center.lon);
  if (lat === undefined || lon === undefined) return null;

  const sport = (tags.sport || 'other').split(';')[0].trim();
  const prettySport = sport.charAt(0).toUpperCase() + sport.slice(1);

  // The app uses the court title as the Firestore document id and the map
  // marker title, so it must be unique and contain no '/'.
  const baseName = (tags.name || `${prettySport} court`).replace(/\//g, '-').trim();
  const title = `${baseName} [OSM ${element.id}]`;

  const details = [];
  details.push(`${prettySport} court`);
  if (tags.surface) details.push(`surface: ${tags.surface}`);
  if (tags.hoops) details.push(`hoops: ${tags.hoops}`);
  if (tags.lit === 'yes') details.push('lit at night');
  if (tags.access && tags.access !== 'yes') details.push(`access: ${tags.access}`);
  details.push(ATTRIBUTION);

  return {
    id: title,
    data: {
      title: title,
      description: details.join(' · '),
      state: 'unknown',
      sport: sport,
      source: 'osm',
      osmId: element.id,
      _lat: lat,
      _lon: lon,
    },
    sport: sport,
  };
}

async function main() {
  const args = parseArgs();
  console.log(`Mode: ${args.write ? 'WRITE' : 'dry run'} · sports: ${args.sports.join(', ')} · limit/sport: ${args.limit}\n`);

  const elements = await fetchOsmCourts(args.sports);
  console.log(`Overpass returned ${elements.length} raw elements.`);

  const courts = elements.map(toCourt).filter(Boolean);

  // Cap per sport so the app map stays usable.
  const bySport = {};
  for (const court of courts) {
    (bySport[court.sport] = bySport[court.sport] || []).push(court);
  }
  const selected = [];
  for (const sport of Object.keys(bySport)) {
    const list = bySport[sport];
    // Prefer named courts - they make better titles than "Soccer court [OSM ...]".
    list.sort((a, b) => (a.id.startsWith(`${a.sport.charAt(0).toUpperCase()}`) ? 1 : 0)
      - (b.id.startsWith(`${b.sport.charAt(0).toUpperCase()}`) ? 1 : 0));
    selected.push(...list.slice(0, args.limit));
    console.log(`  ${sport}: ${list.length} found, importing up to ${Math.min(list.length, args.limit)}`);
  }

  if (!args.write) {
    console.log('\nDry run - nothing written. Sample of what would be imported:');
    for (const court of selected.slice(0, 10)) {
      console.log(`  · ${court.id}  (${court.data._lat.toFixed(5)}, ${court.data._lon.toFixed(5)})`);
    }
    console.log(`\nTotal that would be written: ${selected.length}`);
    console.log('Re-run with --write to import.');
    return;
  }

  // --- Firestore ---
  const keyPath = path.join(__dirname, 'serviceAccountKey.json');
  if (!fs.existsSync(keyPath)) {
    console.error('\nMissing scripts/serviceAccountKey.json.');
    console.error('Firebase console -> Project settings -> Service accounts -> Generate new private key.');
    process.exit(1);
  }
  const admin = require('firebase-admin');
  admin.initializeApp({ credential: admin.credential.cert(require(keyPath)) });
  const db = admin.firestore();

  // Idempotency: skip OSM elements already imported in a previous run.
  const existing = await db.collection('courts').where('source', '==', 'osm').get();
  const existingOsmIds = new Set();
  existing.forEach(doc => existingOsmIds.add(doc.get('osmId')));
  console.log(`\n${existingOsmIds.size} OSM courts already in Firestore; they will be skipped.`);

  let written = 0;
  let skipped = 0;
  let batch = db.batch();
  let batchSize = 0;

  for (const court of selected) {
    if (existingOsmIds.has(court.data.osmId)) { skipped++; continue; }
    const { _lat, _lon, ...data } = court.data;
    data.location = new admin.firestore.GeoPoint(_lat, _lon);
    batch.set(db.collection('courts').doc(court.id), data);
    written++;
    batchSize++;
    if (batchSize === 400) {
      await batch.commit();
      batch = db.batch();
      batchSize = 0;
      console.log(`  ...committed ${written} so far`);
    }
  }
  if (batchSize > 0) await batch.commit();

  console.log(`\nDone. Imported ${written} courts, skipped ${skipped} already-imported.`);

  // Backfill sport field on existing OSM courts that don't have it yet.
  let updated = 0;
  const allOsm = await db.collection('courts').where('source', '==', 'osm').get();
  const updateBatch = db.batch();
  let updateBatchSize = 0;
  for (const doc of allOsm.docs) {
    if (doc.get('sport')) continue;
    const desc = doc.get('description') || '';
    let sport = 'other';
    for (const s of ['basketball', 'soccer', 'tennis', 'volleyball']) {
      if (desc.toLowerCase().includes(s)) { sport = s; break; }
    }
    updateBatch.update(doc.ref, { sport: sport });
    updated++;
    updateBatchSize++;
    if (updateBatchSize === 400) {
      await updateBatch.commit();
      updateBatchSize = 0;
    }
  }
  if (updateBatchSize > 0) await updateBatch.commit();
  if (updated > 0) console.log(`Backfilled sport field on ${updated} existing courts.`);

  console.log('Open the app map to see them with sport-colored markers.');
}

main().catch(err => {
  console.error('\nImport failed:', err.message);
  process.exit(1);
});
