#!/bin/zsh
set -euo pipefail

task_script_dir=${0:A:h}
task_project="$task_script_dir/ReelGuard.xcodeproj"

if [[ -d /Applications/Xcode.app/Contents/Developer ]]; then
    export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
fi

if ! xcodebuild -version >/dev/null 2>&1; then
    print -r -- 'Full Xcode is required. Apple Command Line Tools alone cannot build an iPhone app.'
    print -r -- 'Choose a version compatible with your Mac and iPhone:'
    print -r -- 'https://developer.apple.com/xcode/system-requirements'
    print -r -- 'https://developer.apple.com/download/all/?q=Xcode'
    print -r -- 'After installing Xcode and its iOS platform support, run this helper again.'
    exit 2
fi

task_sdks=$(xcodebuild -showsdks)
if [[ "$task_sdks" != *iphoneos* ]]; then
    print -r -- 'Xcode is present, but its iOS SDK is missing. Install iOS platform support in Xcode Settings.'
    exit 3
fi

print -r -- 'In Xcode: Settings > Apple Accounts > add your personal Apple Account.'
print -r -- 'In ReelGuard: Signing & Capabilities > Automatically manage signing > your Personal Team.'
print -r -- 'Use a unique Bundle Identifier if Xcode reports a conflict.'
print -r -- 'Connect and unlock your iPhone, trust this Mac, select the phone, and press Run.'
print -r -- 'Enable Developer Mode on the iPhone when Xcode asks.'
print -r -- 'Apple free provisioning expires after seven days; rebuild and reinstall to renew it.'
print -r -- 'Enter credentials only in Apple’s sign-in interface. This helper never requests or stores them.'
open "$task_project"
