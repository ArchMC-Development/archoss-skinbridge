# SkinBridge
Services to bridge Eaglercraft & Minecraft Java skins through MineSkin.

## Why?
- Eaglercraft uses its protocol for client skins. Eagler players are not online players, so they cannot create signed skins.
- On networks where the majority of the player base is playing on Eaglercraft, Java players may have an unauthentic experience looking at steve skins.
<img width="435" height="313" alt="CleanShot 2025-08-06 at 22 56 17@2x" src="https://github.com/user-attachments/assets/a1c1eb16-53e2-4a33-9be7-5007461c0066" />

## How?
- Using the MineSkin API, SkinBridge queues **Conversion Jobs** (respecting the max concurrent job limit as described by the MineSkin API limits).
- These conversion jobs are processed by MineSkin, and a response is sent back to the SkinBridge agent, notifying it of a job completion.
- A **Conversion Job** consists of:
  - Converting the Eaglercraft-formatted skin (ABGR8) into a standard PNG skin that MineSkin can process.
  - Sending the job through MineSkin, which uses an online Minecraft account in its pool to generate a valid skin value and signature.
  - Receiving the MineSkin response with the skin value and signature.
  - Sending a one-way RPC network-wide with the valid skin value and signature, forcing an update on the Eagler player's client if online.
  - Caching the response for up to 1w so that the player with the skin (and any other player with that skin) can reuse the generated skin value/signature when they log in again. The caching layer that ArchMC uses is Redis.
- SkinBridge exposes metrics to VictoriaMetrics for monitoring purposes:
<img width="3024" height="1294" alt="CleanShot 2025-08-06 at 23 03 52@2x" src="https://github.com/user-attachments/assets/8a284200-657c-494c-b83a-f7435b8ae62a" />
