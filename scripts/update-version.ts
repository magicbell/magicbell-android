import { replaceInFile } from 'replace-in-file';

import pkgJson from '../package.json';

const version = pkgJson.version

// Warning:
// Make sure to bundle replacements in the same file into one, otherwise the `Promise.all`
// later on can lead to race conditions, overwriting previously applied replacements

const replacements = [
  {
    files: 'README.md',
    from: [
      // Gradle instructions
      /implementation 'com.magicbell:magicbell-sdk:\d\.\d\.\d'/g,
      /implementation 'com.magicbell:magicbell-sdk-compose:\d\.\d\.\d'/g,
    ],
    to: [
      `implementation 'com.magicbell:magicbell-sdk:${version}'`,
      `implementation 'com.magicbell:magicbell-sdk-compose:${version}'`
    ],
  },
  {
    files: 'gradle-mvn-push.gradle',
    from: /coordinates\("com.magicbell", null, "\d\.\d\.\d"\)/g,
    to: `coordinates("com.magicbell", null, "${version}")`,
  }
]

await Promise.all(
  replacements.map(options => replaceInFile(options))
).catch(e => {
  process.stdout.write(`Error updating version via update-version.ts: ${e}\n`);
  process.exit(1);
})