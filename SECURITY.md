# Security Policy

## Supported Versions

PadConnect is under active development. Only the latest released version is supported with security fixes. Please make sure you're on the most recent release before reporting an issue.

| Version | Supported |
| ------- | --------- |
| Latest release | ✅ |
| Older releases | ❌ |

## Reporting a Vulnerability

If you discover a security vulnerability in PadConnect, please **do not open a public GitHub issue**. Instead:

1. Report it privately via **GitHub's [private vulnerability reporting](https://github.com/Ishan09811/PadConnect/security/advisories/new)** feature on this repository (Security tab -> "Report a vulnerability"), or
2. Reach out on the [Discord server](https://discord.gg/BrMAZbEyXs) and request a private channel with a maintainer.

Please include:
- A description of the vulnerability and its potential impact
- Steps to reproduce (proof-of-concept if possible)
- Affected version(s) of PadConnect and, if relevant, [PadConnectReceiver](https://github.com/Ishan09811/PadConnectReceiver)
- Your Android version/device, if relevant to reproduction

You should receive an initial response within a few days. This is a small open-source project maintained in spare time, so please be patient, response and fix timelines aren't guaranteed, but reports are taken seriously.

## Scope and Known Risk Areas

PadConnect streams controller input over **UDP on the local network** to [PadConnectReceiver](https://github.com/Ishan09811/PadConnectReceiver). Relevant security considerations:

- **No built in encryption or authentication on the input stream** so anyone on the same local network can potentially observe or inject UDP packets targeting the receiver's listening port. Only use this on networks you trust.
- The app requests local network / WiFi permissions in order to discover and stream to the receiver. It does not require or request internet access for core functionality.
- Vulnerabilities in how untrusted UDP input is parsed on the receiver side (buffer handling, malformed packets) are in scope, please report these even though the parsing code lives in the [PadConnectReceiver](https://github.com/Ishan09811/PadConnectReceiver) repo, since it directly affects PadConnect users.

## Out of Scope

- Issues that require physical access to an already unlocked device
- Social engineering attacks
- Vulnerabilities in third party dependencies that have no practical exploit path through PadConnect itself (please report these upstream as well)

## Disclosure

We ask that you give us a reasonable opportunity to investigate and fix a reported vulnerability before any public disclosure. Credit will be given in release notes for responsibly disclosed issues, unless you prefer to remain anonymous.
