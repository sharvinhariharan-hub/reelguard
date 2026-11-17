#!/usr/bin/env python3
"""Exercise synthetic native surfaces on a disposable emulator, never a physical phone."""
import argparse
import os
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('--serial', default='emulator-5554')
args = parser.parse_args()
if not args.serial.startswith('emulator-'):
    raise SystemExit('Refusing a non-emulator device: the fixture impersonates Instagram’s package for testing.')
root = Path(__file__).resolve().parents[1]
sdk = Path(os.environ.get('ANDROID_HOME', Path.home() / 'Library/Android/sdk'))
adb = [str(sdk / 'platform-tools/adb'), '-s', args.serial]

def run(*parts):
    return subprocess.check_output([*adb, *parts], text=True, stderr=subprocess.STDOUT).strip()

def ui():
    run('shell', 'uiautomator', 'dump', '/sdcard/reelguard-test.xml')
    return ET.fromstring(run('shell', 'cat', '/sdcard/reelguard-test.xml'))

def find(label, timeout=15):
    end = time.monotonic() + timeout
    while time.monotonic() < end:
        tree = ui()
        for node in tree.iter('node'):
            if node.attrib.get('text', '').casefold() == label.casefold():
                return node
        time.sleep(.3)
    raise AssertionError(f'Expected visible label: {label}')

def tap(label):
    bounds = find(label).attrib['bounds']
    l, t, r, b = map(int, re.findall(r'\d+', bounds))
    run('shell', 'input', 'tap', str((l+r)//2), str((t+b)//2))

def activity(component, *extras):
    run('shell', 'am', 'start', '-W', '-f', '0x14000000', '-n', component, *extras)
    time.sleep(.5)

def overlay_visible():
    # Android's stock uiautomator dump omits non-focusable accessibility overlays.
    return 'ReelGuard Reels blocker' in run('shell', 'dumpsys', 'window')

def expect_overlay(visible):
    end = time.monotonic() + 8
    while time.monotonic() < end:
        if overlay_visible() == visible:
            return
        time.sleep(.2)
    raise AssertionError(f'Expected blocker window visible={visible}')

fixture = 'com.instagram.android/.FixtureActivity'
app = 'dev.reelguard.app/.MainActivity'
service = 'dev.reelguard.app/.ReelBlockService'
if '1080x2400' not in run('shell', 'wm', 'size'):
    raise SystemExit('This smoke script uses a visually verified 1080x2400 overlay button position.')
existing = run('shell', 'pm', 'list', 'packages', 'com.instagram.android').splitlines()
if 'package:com.instagram.android' in existing:
    resolved = run('shell', 'cmd', 'package', 'resolve-activity', '--brief', 'com.instagram.android')
    if 'FixtureActivity' not in resolved:
        raise SystemExit('Refusing to overwrite an existing Instagram installation.')
run('install', '-r', str(root / 'app/build/outputs/apk/debug/app-debug.apk'))
run('install', '-r', str(root / 'fixture/build/outputs/apk/debug/fixture-debug.apk'))
for setting in ('window_animation_scale', 'transition_animation_scale', 'animator_duration_scale'):
    run('shell', 'settings', 'put', 'global', setting, '0')
run('shell', 'input', 'keyevent', 'KEYCODE_WAKEUP')
run('shell', 'wm', 'dismiss-keyguard')
activity(app)
tap('1. Review accessibility access')
tap('I agree')
enabled = run('shell', 'settings', 'get', 'secure', 'enabled_accessibility_services')
services = [] if enabled in ('', 'null') else enabled.split(':')
if service not in services:
    services.append(service)
run('shell', 'settings', 'put', 'secure', 'enabled_accessibility_services', ':'.join(services))
run('shell', 'settings', 'put', 'secure', 'accessibility_enabled', '1')
time.sleep(1)

activity(fixture)
find('TEST PHOTO FEED')
print('PASS: photo feed remains accessible')
tap('Open DMs')
find('TEST DM CONVERSATION')
print('PASS: DMs remain accessible')
tap('Open Reels')
find('TEST DM CONVERSATION')
print('PASS: Reel opened from DM returns to DM')
tap('Open Story')
find('TEST STORY')
time.sleep(1)
find('TEST STORY')
print('PASS: Story with preloaded offscreen Reels player remains accessible')

# A viewer that ignores Back keeps the overlay up so its controls can be exercised.
activity(fixture, '--es', 'screen', 'reels', '--ez', 'trap', 'true')
expect_overlay(True)
print('PASS: Back failure leaves an actionable blocker')
run('shell', 'screencap', '-p', '/sdcard/reelguard-blocker.png')
run('pull', '/sdcard/reelguard-blocker.png', str(root.parent / 'reelguard-blocker.png'))
# Position verified from the overlay screenshot on this 1080x2400 emulator.
run('shell', 'input', 'tap', '540', '1400')
expect_overlay(False)
find('TEST REELS PLAYER')
print('PASS: overlay off switch dismisses blocker and allows player')

activity(app)
tap('Block Reels')
activity(fixture, '--es', 'screen', 'reels', '--ez', 'trap', 'true')
expect_overlay(True)
run('shell', 'input', 'keyevent', 'KEYCODE_HOME')
time.sleep(1)
expect_overlay(False)
print('PASS: leaving Instagram clears overlay')
activity(app)
tile = 'dev.reelguard.app/.GuardTileService'
run('shell', 'cmd', 'statusbar', 'add-tile', tile)
run('shell', 'cmd', 'statusbar', 'expand-settings')
time.sleep(1)
run('shell', 'cmd', 'statusbar', 'click-tile', tile)
run('shell', 'cmd', 'statusbar', 'collapse')
activity(app)
find('Blocking is off')
print('PASS: Quick Settings tile turns blocking off')
run('shell', 'cmd', 'statusbar', 'expand-settings')
time.sleep(1)
run('shell', 'cmd', 'statusbar', 'click-tile', tile)
run('shell', 'cmd', 'statusbar', 'collapse')
activity(app)
find('Blocking requested · accessibility enabled')
print('PASS: Quick Settings tile turns blocking on')
run('shell', 'screencap', '-p', '/sdcard/reelguard-home.png')
run('pull', '/sdcard/reelguard-home.png', str(root.parent / 'reelguard-android.png'))
print('Synthetic emulator smoke checks complete. Live Instagram compatibility is unverified.')
