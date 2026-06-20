# SignPicture 1.12.2 Modification Memory

## User Goal

Modify the Minecraft 1.12.2 version of SignPicture so online sign images can load/render from 128 blocks away, and provide a jar that can be placed directly into the `mods` folder.

## Distance Research

- SignPicture does not have its own configurable image loading distance.
- Image loading is triggered during sign tile entity rendering:
  - `CustomBlockSignRenderer.render(...)`
  - `SignEntryId.fromTile(tile).entry()`
  - `entry.getGui()`
  - `GuiImage.renderSignPicture(...)`
  - `entry.getContent()`
  - `ContentManager.get(...)`
- Minecraft 1.12.2 limits tile entity rendering through `TileEntity#getMaxRenderDistanceSquared()`.
- The vanilla default is `4096.0D`, which is `64 * 64`.
- `TileEntityRendererDispatcher` checks distance using a strict `< getMaxRenderDistanceSquared()` condition.
- SignPicture already patched `TileEntity#getRenderBoundingBox()` for signs to use `INFINITE_EXTENT_AABB`; that affects frustum culling, not distance.

## Source Changes

Changed files:

- `gradle.properties`
  - Changed version from `3.0.0` to `3.1.0`.
- `common/project.gradle`
  - Changed BnnWidget from a compile-only dependency to a shaded dependency for packaging work.
- `sources/universal/src/main/java/net/teamfruit/signpic/asm/ASMDeobfNames.java`
  - Added deobfuscated/obfuscated method name mapping for `TileEntity#getMaxRenderDistanceSquared()`.
- `sources/universal/src/main/java/net/teamfruit/signpic/asm/TileEntityVisitor.java`
  - Kept the existing render bounding box hook.
  - Added a hook for `getMaxRenderDistanceSquared()`.
  - For `TileEntitySign`, the hook returns `16384.0D`, which is `128 * 128`.

## Build Notes

- Use Java 8:
  - `/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home`
- `./gradlew` is not executable in this checkout; use `bash gradlew`.
- Old Gradle/plugin dependencies may require proxy variables:
  - `http_proxy=http://127.0.0.1:7897`
  - `https_proxy=http://127.0.0.1:7897`
  - `HTTP_PROXY=http://127.0.0.1:7897`
  - `HTTPS_PROXY=http://127.0.0.1:7897`
- A local Maven cache was used for old `http-builder:0.7.2`:
  - `build/local-maven/org/codehaus/groovy/modules/http-builder/http-builder/0.7.2/`
- Temporary `settings.gradle` edits were used during builds to include only `common` and `1.12.2`; `settings.gradle` was restored afterward.

## Crash Fixes

### First Crash

Crash report:

- `/Volumes/Shared/crash-2026-06-20_13.34.07-client.txt`

Cause:

- `NoClassDefFoundError: net/teamfruit/bnnwidget/WFrame`
- The first jar did not include BnnWidget, and the user's `mods` folder did not contain a standalone BnnWidget jar.

Fix attempt:

- Embedded BnnWidget classes and resources into the final jar.

### Second Crash

Crash report:

- `/Volumes/Shared/crash-2026-06-20_13.43.34-client.txt`

Cause:

- `NoSuchMethodError: net.minecraft.client.Minecraft.getMinecraft()`
- The embedded BnnWidget was the dev/deobfuscated jar.
- The user's Cleanroom/Forge runtime needs obfuscated runtime names such as `Minecraft.func_71410_x()`.

Final fix:

- Rebuilt the final jar using SignPicture 3.1.0 output as the base.
- Replaced the embedded BnnWidget contents with the reobfuscated BnnWidget classes/resources extracted from:
  - `artifacts/3.0.0/3.0.0+001/SignPicture-1.12.2-3.0.0-cleanroom-fat.jar`
- Verified `net.teamfruit.bnnwidget.compat.Compat#getMinecraft()` calls:
  - `net/minecraft/client/Minecraft.func_71410_x()`

## Final Jar

Final deliverable:

- `artifacts/SignPicture-1.12.2-3.1.0-128range.jar`

SHA-256:

```text
972923b39c097b5bff3c2c91a0183123ab212e57195689ad38e389706a002c01
```

Verified:

- `net/teamfruit/bnnwidget/WFrame.class` exists in the jar.
- `net/teamfruit/bnnwidget/compat/Compat.class` uses runtime names.
- `mcmod.info` remains SignPicture metadata.
- `@Mod` version is `3.1.0`.
- The ASM hook still contains distance constant `16384.0D`.
- The jar has no duplicate zip entries.

