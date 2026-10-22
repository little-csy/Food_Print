# FoodPrint

A cross-platform mobile application frontend built with Expo and React Native.

## Requirements

- **Node.js:** v18 or later recommended ([official website](https://nodejs.org/)).
- **pnpm:** used to manage project dependencies.

    ```bash
    npm install -g pnpm
    ```

- **Expo Go** (physical device debugging): search for "Expo Go" in the phone app store and install it.

## Quick Start

### 1. Clone and enter the project directory

```bash
git clone <Repository URL>
cd FoodPrint
```

### 2. Install dependencies

```bash
pnpm install
```

### 3. Start the development server

```bash
pnpm start
```

The terminal displays a QR code and Metro address. You can:

- **Physical device:** scan the QR code with Expo Go to open the app.
- **Emulator:** press `a` for Android, or `i` for the iOS simulator (requires macOS and Xcode).
- **Web:** press `w` to open in a browser.

## Common Scripts

| Command                | Description                               |
| ------------------- | ---------------------------------- |
| `pnpm start` | Start the Expo development server |
| `pnpm android` | Start in Android mode |
| `pnpm ios` | Start in iOS mode (macOS only) |
| `pnpm web` | Start in Web mode |
| `pnpm lint` | Run ESLint checks |
| `pnpm lint:fix` | Automatically fix supported ESLint issues |
| `pnpm format` | Format code with Prettier |
| `pnpm format:check` | Check formatting against Prettier rules |

## Project Structure Overview

```
FoodPrint/
├── App.tsx              # App entry point; mounts navigation only
├── src/
│   ├── navigation/      # Navigation configuration (tabs, routes, etc.)
│   │   └── AppNavigator.tsx
│   └── screens/         # Screen components
│       ├── HomeScreen.tsx
│       └── ProfileScreen.tsx
├── assets/              # Static assets such as images and icons
└── package.json
```

To add a screen, create a component under `src/screens` and register its route in `src/navigation/AppNavigator.tsx`.

## Documentation

- [Git Workflow and Code Review](docs/GIT_WORKFLOW.md): branches, commits, PRs, and review process
- [Style Management](docs/STYLE_GUIDE.md): themes, design tokens, screen styles, and how to modify them

## Git And Collaboration

For team development, follow [Git Workflow and Code Review](docs/GIT_WORKFLOW.md), including branch creation, commit conventions, pull requests, reviews, and merging.

## Development Recommendations

- Before committing, run `pnpm lint` and `pnpm format:check`, or `pnpm lint:fix` and `pnpm format` to keep code style consistent.
- Use TypeScript and the existing ESLint / Prettier configuration; avoid arbitrary formatting rule changes.

## Frequently Asked Questions

- **Dependency installation fails:** confirm Node ≥ 18 and pnpm is installed globally, then run `pnpm install`.
- **QR code connection fails:** ensure the phone and computer share a LAN; if needed, disable the VPN or firewall and try again.
- **iOS simulator:** available only on macOS; install Xcode and iOS Simulator first.
