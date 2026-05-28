# Task: ProgressViewActivity.kt — Thin UI Shell

## Task ID
`progress-view-activity-001`

## Agent
Chief Android Architect

## Summary
Created `ProgressViewActivity.kt` as a thin UI shell for the QuranMaker-Kotlin project, converting the original 1212-line Java `ProgressViewActivity` into a ~970-line Kotlin structural skeleton.

## Files Created/Modified

### Created
- **`app/src/main/java/hazem/nurmontage/videoquran/ui/render/ProgressViewActivity.kt`** (977 lines)

### Modified
- **`app/src/main/java/hazem/nurmontage/videoquran/ui/render/ExportCommandBuilder.kt`**
  - Added `EntityMedia` and `FfmpegCodecChecker` imports
  - Added `PreRenderStep` functional interface
  - Added `buildCommand()` method — orchestrates the full export pipeline with lambda callbacks for pre-render steps
  - Added `buildFinalCommand()` private method — composes the final FFmpeg filter_complex overlay chain

## Key Design Decisions

### Threading Model
- **Replaced**: `Handler` / `Runnable` / `Executor` / `Thread` → Kotlin Coroutines
- **Pattern**: `lifecycleScope.launch(Dispatchers.IO)` for background work, `withContext(Dispatchers.Main)` for UI updates
- **Preserved**: `CountDownLatch`/`Semaphore` in pre-render method signatures for `ExportCommandBuilder.buildCommand()` coordination — these bridge the coroutine world with FFmpegKit's callback-based async API

### Architecture
- **Delegation**: FFmpeg command composition is delegated to `ExportCommandBuilder.buildCommand()` rather than embedded in the Activity
- **Pre-render execution**: Stays in `ProgressViewActivity` since it calls `FFmpegKit.executeWithArgumentsAsync()` and manages session IDs
- **Fade/slide helpers**: Delegated to `ExportCommandBuilder` static methods (mFadeFilter, fadeInOut, slideX, mSlideX)

### Cancel Dialog
- Named `showCancelDialog()` to avoid field/method name collision with `cancelDialog` field
- Arabic/English localization preserved exactly from Java source

### Wake Lock
- Uses `PowerManager.PARTIAL_WAKE_LOCK` with 10-minute timeout
- Falls back to `BaseActivity.wakeLockAcquire()` (window flag) on failure
- Proper release in `onDestroy()` via `releaseWakeLock()`

## Required Methods Checklist
- [x] `startExport()` — reads Template from intent, prepares media, detects codecs
- [x] `setupCommand(codecInfo)` — delegates to ExportCommandBuilder.buildCommand()
- [x] `showCancelDialog()` — Arabic/English cancel confirmation dialog
- [x] `export(command)` — executes FFmpeg with progress/stats callbacks
- [x] `updateProgressDialog(stats)` — RenderManager-weighted progress calculation
- [x] `clearFFmpeg()` — cancels all tracked FFmpeg sessions
- [x] `prepareAllMedia(list, callback)` — coroutine-based media download/copy
- [x] `toStudio()` — navigates back with RESULT_CANCELED
- [x] `onExportComplete()` — navigates back with RESULT_OK + output URI
- [x] `preRenderMask_Rounded()` — rounded-rect masked segment
- [x] `preRenderMask_Circle()` — circle masked segment
- [x] `preRender_NoMask()` — direct overlay segment
- [x] `preRenderVideo()` — main video + background overlay
- [x] `preRenderVideoHue()` — hue-shifted video layer
- [x] `generateVideoTimer()` — drawtext timer overlay
- [x] `createTransparentBg()` — transparent PNG generator
- [x] `concatVideoSegments()` — FFmpeg concat demuxer
- [x] `updateNext(latch, semaphore)` — RenderManager + coordination signal
- [x] Fade/slide delegate methods (mFadeFilter, fadeInOut, slideX, mSlideX)

## TODOs (Future Work)
- Replace `progressIndicator: View` with `SquareOutlineProgressBar` custom view
- Complete `buildFinalCommand()` filter_complex with exact overlay expressions, fade timings, and positioning from original Java
- Layout file `R.layout.activity_progress_view` needs to be created with `R.id.main`, `R.id.progress_horizontal`, `R.id.btn_cancel`
