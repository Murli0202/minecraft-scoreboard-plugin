# 📊 Scoreboard Plugin (Paper 26.1.x)

A lightweight Paper plugin that shows a fully customizable sidebar scoreboard with money, MurliTokens, rank, kills, deaths and ping. All data persists across server restarts.

---

## ✅ Requirements

- Paper Server (26.1.x)
- Java 21
- [Vault](https://www.spigotmc.org/resources/vault.34315/) + economy plugin like [EssentialsX](https://essentialsx.net/) *(optional but recommended)*

---

## 💰 Economy / Vault Integration

The plugin automatically hooks into **Vault** if it is installed. This means the `{money}` placeholder on the scoreboard shows the player's balance from your existing economy plugin (EssentialsX, CMI, etc.).

| Situation | What happens |
|---|---|
| Vault + EssentialsX installed | Uses EssentialsX balances, formatted by Vault |
| Vault installed but no economy plugin | Falls back to built-in money system |
| Vault not installed | Uses built-in money system (saved in `money.yml`) |

To use Vault: just drop `Vault.jar` and your economy plugin into `/plugins/` — no config needed.

---

## 📦 Installation

### Option A – Build it yourself

```bash
git clone https://github.com/Murli0202/scoreboard-plugin.git
cd scoreboard-plugin
mvn package -q
cp target/ScoreboardPlugin-1.0.jar /path/to/server/plugins/
```

### Option B – Ready-made JAR

1. Download `ScoreboardPlugin-1.0.jar` from the [Releases](../../releases) tab
2. Copy the JAR into your `/plugins/` folder
3. Restart the server

---

## 📋 Features

- **Sidebar scoreboard** shown to every player automatically
- **Money system** — starting balance configurable, persists on restart
- **MurliTokens** — custom currency manageable via commands
- **Ranks** — permission-based ranks shown on the scoreboard
- **Kills & Deaths** — tracked per player, saved permanently
- **Ping** — live ping shown per player
- **Fully configurable** — change everything in `config.yml`
- **Auto-save** every 5 minutes + save on every change + save on shutdown

---

## ⚙️ Configuration

After the first start, edit `plugins/ScoreboardPlugin/config.yml`:

```yml
title: "&6&lMurli Network"        # Scoreboard title
update-interval-ticks: 20         # Refresh rate (20 = 1 second)
starting-money: 100.0             # Money for new players
money-symbol: "$"                 # Symbol before money amounts

# Scoreboard lines — available placeholders:
# {player} {money} {tokens} {kills} {deaths} {ping} {players} {rank}
lines:
  - "&eMoney: &a{money}"
  - "&eMurliTokens: &6{tokens}"
  - "&eKills: &a{kills}"
  - "&eDeaths: &c{deaths}"
  - "&ePing: &b{ping}"

# Permission-based ranks (first match wins)
ranks:
  - permission: "rank.owner"
    display: "&4&lOwner"
  - permission: "rank.admin"
    display: "&c&lAdmin"
```

---

## 🎮 Commands

| Command | Description | Permission |
|---|---|---|
| `/murlitoken give <player> <amount>` | Give tokens to a player | `scoreboard.token.admin` |
| `/murlitoken take <player> <amount>` | Take tokens from a player | `scoreboard.token.admin` |
| `/murlitoken set <player> <amount>` | Set a player's tokens | `scoreboard.token.admin` |
| `/murlitoken check <player>` | Check a player's tokens | `scoreboard.token.admin` |

All commands require OP or the `scoreboard.token.admin` permission.

---

## 🏅 Ranks

Ranks are assigned via permissions. Give a player the permission and they'll see their rank on the scoreboard.

| Permission | Default Rank |
|---|---|
| `rank.owner` | Owner |
| `rank.admin` | Admin (OPs) |
| `rank.mod` | Mod |
| `rank.vip` | VIP |
| `rank.member` | Member (all players) |

You can add, remove, or rename ranks freely in `config.yml`.

---

## 🔔 Update Notifications

When a new version is released on GitHub, any OP player will automatically receive a message when they join the server — no manual checking needed!

```
[ScoreboardPlugin] New version available: v1.1.0
Current: v1.0.0 → New: v1.1.0
Download: https://github.com/Murli0202/scoreboard-plugin/releases/latest
```

---

## 💾 Data Storage

All data is saved in `plugins/ScoreboardPlugin/`:

| File | Contents |
|---|---|
| `tokens.yml` | MurliTokens per player |
| `stats.yml` | Kills & deaths per player |
| `money.yml` | Money balances per player |

Data is saved on every change, every 5 minutes automatically, and on server shutdown — so nothing is lost even if the server crashes.

---

## 📄 License

MIT – do whatever you want with it!
