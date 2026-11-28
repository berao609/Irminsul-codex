# Genshin-DB data attribution

The bundled character and talent progression cost records in this directory are derived from Genshin-DB.

- Project: https://github.com/theBowja/genshin-db
- Upstream commit: 8e5badd60b5d894284ccc927a56c9683529d2592
- Game data version represented by this snapshot: 7.2
- Upstream license: MIT
- Copyright: Copyright (c) 2020 theBowja
- License text: https://github.com/theBowja/genshin-db/blob/8e5badd60b5d894284ccc927a56c9683529d2592/LICENSE

The application retains the upstream copyright and permission notice. The bundled records are narrowed to character IDs, names, rarity, ascension costs, and talent costs, then divided into numbered JSON chunks for Android asset loading.

These records are still candidate game data. MIT permission to redistribute the upstream dataset does not establish that every game fact has been independently verified. The application therefore preserves the candidate-data boundary and does not promote these records into trusted production costs automatically.

No image binaries are included in these data files. The app also bundles `enhancement_visual_metadata.json`, a small ID-to-asset metadata map derived from EnkaNetwork/API-docs commit `26b4ddcd045a1264d0a8ef3ef5a3a3e33cbe16ce` (`store/gi/avatars.json` and `store/gi/weapons.json`). It maps character/weapon IDs to element, weapon type, and Enka CDN asset URLs. Image files are requested from the Enka CDN at runtime; this metadata attribution does not claim ownership or grant a license to HoYoverse artwork.
