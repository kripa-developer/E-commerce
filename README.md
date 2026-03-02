# NovaCart - Phase 1 Login UI

This repository contains the first step of the e-commerce front-end: a modern, interactive login experience using HTML, CSS, and JavaScript.

## What's included
- Responsive split-layout hero + login card design.
- Username/password sign-in + new account registration flow with inline validation.
- Show/hide password toggle.
- "Remember me", registered users, and session state using `localStorage`.
- Forgot-password flow to reset password by username.
- Accessible form semantics and live status messaging.
- Advanced cinematic animations: floating background orbs, shimmering heading, 3D tilt card interaction, and button ripple feedback.

## Run locally (npm workflow)
```bash
npm start
```
Then open `http://localhost:4173`.

## Why Python was used earlier
A simple Python static server was only used as a quick way to preview static HTML/CSS/JS. This repo now uses npm scripts so you can stay in a Node/VS Code workflow.

## If you want Angular + TypeScript next
Once you are ready, we can migrate this UI into Angular components (`LoginComponent`) and TypeScript forms (`ReactiveFormsModule`) while keeping the same design/animations.

---

When you're ready, share the next feature and I can build it step by step (catalog, navbar, product cards, cart, checkout, etc.).
