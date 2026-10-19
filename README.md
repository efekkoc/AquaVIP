# 👑 AquaVIP — Enterprise Monetization, Web-Store Sync & Timed Perks Engine

<p align="left">
  <a href="https://github.com/aquaforge-mc"><img src="https://img.shields.io/badge/Holding_Brand-AquaForge%20Ecosystem-00c853?style=for-the-badge&logo=github&logoColor=white" alt="AquaForge Ecosystem" /></a>
  <a href="https://github.com/efekkoc"><img src="https://img.shields.io/badge/Chief%20Architect-%40efekkoc-38bdf8?style=for-the-badge" alt="Chief Architect" /></a>
  <a href="https://github.com/aquaforge-mc"><img src="https://img.shields.io/badge/PaperMC-1.20.x%20--%201.21.11-E53935?style=for-the-badge&logo=curseforge&logoColor=white" alt="PaperMC 1.20.x - 1.21.11" /></a>
  <a href="https://github.com/efekkoc/AquaVIP"><img src="https://img.shields.io/badge/Version-1.0.0%20Release-blueviolet?style=for-the-badge" alt="Version 1.0.0" /></a>
</p>

**AquaVIP** is a high-performance donor subscription, monetization, and automated perk distribution engine engineered as the dedicated **Monetization & Perks Division** of the **[AquaForge Ecosystem](https://github.com/aquaforge-mc)**. Universal compatibility across **Paper 1.20.x through 1.21.11 (Mounts of Mayhem Ready)** with zero external dependencies.

---

### ⚡ Architectural Features

* 🛒 **Web-Store Automation (CraftingStore, Tebex, Keyubu, MineStore):**
  * Console delivery commands: `/givevip <player> <tier> <duration>` (e.g. `givevip Steve VIP 30d`, `givevip Alex MVP permanent`).
  * Automatic duration stacking: If a player purchases VIP twice, the expiration date safely extends rather than resetting.
  * Instant auto-revoke: Background task runs every 60 seconds. When time runs out, flight and perks are automatically disabled, and an alert is dispatched.
* 🚪 **VIP Priority Join (Full Server Bypass):**
  * Allows paying subscribers and VIPs to join even when the server reaches 100/100 slots (`PlayerLoginEvent.Result.KICK_FULL` bypass).
* 🧰 **All-in-One Donor Perks Suite:**
  * 🪽 `/fly`: Donor flight toggle.
  * 🍗 `/feed`: Hunger replenishment with configurable cooldown.
  * 💖 `/heal`: Health restoration, fire tick clearing, and negative effect cleansing with cooldown.
  * 🛠️ `/craft` & `/anvil`: Virtual crafting table and anvil accessible anywhere.
  * 📦 `/enderchest` (`/ec`): Virtual ender chest access.
  * 🎩 `/hat`: Wear any block or item from hand as an overhead decorative helmet.
  * 📡 `/near`: Radar scanning nearby players within configurable tier radius.
* 🎁 **VIP Kit System with Cooldowns:**
  * Multi-tier kit claims (`/vipkit VIP`, `/vipkit MVP`, `/vipkit AQUA_LEGEND`) with customizable cooldown hours.
* 🎆 **Cosmetic Join Broadcasts & Fireworks:**
  * Colored server-wide chat broadcasts and celebratory multi-colored firework displays upon VIP connection.

---

### 📜 Commands & Permissions

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/vip` | `None` (Public) | View VIP packages, perks, and personal subscription status |
| `/viptime [player]` | `None` (Public) | Check remaining subscription time |
| `/vipkit [tier]` | `aquavip.kit` | Claim tier kit with cooldown |
| `/givevip <player> <tier> <süre>` | `aquavip.admin` | Deliver VIP subscription from web store or console |
| `/takevip <player>` | `aquavip.admin` | Revoke active VIP subscription immediately |
| `/fly` | `aquavip.perk.fly` | Toggle donor flight |
| `/feed` | `aquavip.perk.feed` | Replenish hunger bar |
| `/heal` | `aquavip.perk.heal` | Replenish health and clear debuffs |
| `/craft` | `aquavip.perk.craft` | Open virtual workbench |
| `/anvil` | `aquavip.perk.anvil` | Open virtual anvil |
| `/enderchest` (`/ec`) | `aquavip.perk.enderchest` | Open virtual ender chest |
| `/hat` | `aquavip.perk.hat` | Wear item in hand as a hat |
| `/near` | `aquavip.perk.near` | Scan nearby players within radius |

---

### 🎖️ Official Server Ranks Roster & Visual Badges

| Rank Tier | Visual Badge Style | Theme & Color Palette | Included Perks & Kits |
| :--- | :--- | :--- | :--- |
| **`VIP`** | `✦ [VIP] ✦` | Lime Green (`#7CFC00`) with winged pill badge & star sparkles | `/fly`, `/feed`, `/craft`, `/hat`, Tier I Kit |
| **`VIP_PLUS`** | `❖ [VIP+] ❖` | Dark Emerald (`#00AA00`) with gold border & golden `+` | `/fly`, `/feed`, `/craft`, `/anvil`, `/ec`, `/hat`, Tier II Kit |
| **`MVP`** | `❄ [MVP] ❄` | Frost Ice Cyan (`#38BDF8`) with crystal reflections & ice bursts | `/fly`, `/feed`, `/heal`, `/craft`, `/anvil`, `/ec`, `/hat`, `/near`, Tier III Kit |
| **`MVP_PLUS`** | `⭐ [MVP+] ⭐` | Radiant Gold (`#FFAA00`) with red `+` & star sparkles | Extended `/near` (90m), Netherite gear, Golden Apples |
| **`ELITE`** | `⚡ ‹ELITE› ⚡` | Void Obsidian Purple (`#A855F7`) with winged horns & void aura | Full perk suite, 120m radar, Totem of Undying, Master Netherite Kit |

---

### 📦 Web Store Console Setup Example

In your web-store console command dispatcher (Tebex, CraftingStore, Keyubu):
```bash
# For 30 Days VIP:
givevip {name} VIP 30d

# For 30 Days VIP+:
givevip {name} VIP_PLUS 30d

# For 60 Days MVP:
givevip {name} MVP 60d

# For 60 Days MVP+:
givevip {name} MVP_PLUS 60d

# For Lifetime ELITE:
givevip {name} ELITE permanent
```

---

### 🏢 AquaForge Ecosystem Synergy

AquaVIP seamlessly handshakes with other AquaForge suite components:
* **AquaRanks:** Automatically synchronizes player prefixes and overhead nametags upon `/givevip`.
* **AquaChat:** Unlocks VIP-exclusive chat gradients and symbols.
* **AquaScoreboard:** Live displays `{vip_tier}` and `{vip_time_left}` on the sidebar scoreboard.
* **AquaEconomy:** Grants bonus VIP transaction multipliers.

---

### 🛡️ Author & Licensing
Developed and architected by **Turgut Efekan Koç ([@efekkoc](https://github.com/efekkoc))** under the **AquaForge Ecosystem**.  
Licensed under the Apache 2.0 License.
