# 🔥 HypeUp - TikTok Style Fire Streak Plugin

A Minecraft plugin that brings TikTok's fire streak system to the game! Players can build relationships with friends through fire streaks and earn rewards for consecutive days.

## Commands

### Player Commands

| Command | Description | Permission |
|--------|----------|------------|
| `/hypeup` | Open fire partner list GUI | `hypeup.use` |
| `/hypeup help` | Show help message | `hypeup.use` |
| `/hypeup send <player>` | Open gift GUI to send items | `hypeup.use` |
| `/hypeup info <player>` | View fire streak info with player | `hypeup.use` |
| `/hypeup list` | View all fire partners | `hypeup.use` |
| `/hypeup msg <player> <message>` | Send message for chat mission | `hypeup.use` |
| `/hypeup reload` | Reload configuration | `hypeup.admin` |

### Admin Commands

| Command | Description | Permission |
|--------|----------|------------|
| `/hypeup admin set <player1> <player2> <amount>` | Set fire streak to specific value | `hypeup.admin` |
| `/hypeup admin give <player1> <player2> <amount>` | Add days to fire streak | `hypeup.admin` |
| `/hypeup admin take <player1> <player2> <amount>` | Remove days from fire streak | `hypeup.admin` |
| `/hypeup admin expired <player1> <player2>` | Set fire as expired (streak = 0) | `hypeup.admin` |
| `/hypeup admin extinguish <player1> <player2>` | Extinguish fire but keep streak | `hypeup.admin` |
| `/hypeup admin reset <player1> <player2>` | Reset daily missions | `hypeup.admin` |

## How to Light Fire

To maintain a fire streak with your partner, you must complete **3 daily missions** before midnight:

### Mission Requirements

You must complete **all 3 missions** with your partner each day:

1. **Chat Mission**
   - Send at least **2 messages** to your partner using `/hypeup msg <player> <message>`
   - Messages must have minimum delay between them (default: 5-10 seconds)
   - Must be within configured distance (default: 50 blocks) or -1 for unlimited
   - Both players must send messages to each other
   - Progress shows in chat: "Chat progress with [Player]: 1/2"

2. **Shift Mission**
   - Press **Shift (Sneak)** near your partner at least **2 times**
   - Must be within configured distance (default: 3 blocks)
   - Each shift interaction counts towards progress
   - Progress shows in chat: "Shift interaction with [Player]: 1/2"
   - Both players must complete shift interactions

3. **Gift Mission**
   - Use `/hypeup send <partner>` to open gift GUI
   - Place any items in the GUI slots
   - Click confirm button to send items
   - Items will be transferred to partner's inventory
   - Must be within configured distance (default: 50 blocks) or -1 for unlimited
   - Both players must send gifts to each other

### Lighting the Fire

1. **Choose a Partner:** Use `/hypeup send <player>` or `/hypeup msg <player> <message>`
2. **Complete Missions:** Both you and your partner must complete all 3 missions
   - Chat: Send messages with `/hypeup msg`
   - Shift: Sneak near each other
   - Gift: Send items through GUI
3. **Auto Light:** Fire lights automatically when both players complete all missions
4. **Streak Increase:** Your fire streak increases by 1 day
5. **Daily Requirement:** Must complete before midnight to maintain streak!
6. **Mission Reset:** Missions reset daily at midnight (configured timezone)

**Note:** If you miss a day, your fire will expire. You can restore it within the configured restore period (default: 3 days) up to a maximum number of times (default: 3 restores).

### Streak Mechanics

- **Reset Time:** Midnight (00:00) in configured timezone
- **Restore System:** Can restore expired fire up to 3 times (if within restore days)
- **Fire Colors:** Fire changes color based on streak (Red → Orange → Gold → Rainbow...)
- **Rewards:** Receive rewards at milestone days (7, 14, 30, 60, 90)

## PlaceholderAPI Support

### General Placeholders
| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%hypeup_total_partners%` | Total number of fire partners | `5` |
| `%hypeup_max_streak%` | Highest streak among all partners | `30` |
| `%hypeup_total_active%` | Number of active fire partners | `3` |
| `%hypeup_has_fire%` | Whether player has any fire streak | `true/false` |

### Top Partner Placeholders
| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%hypeup_top_partner%` | Name of partner with highest streak | `Steve` |
| `%hypeup_top_streak%` | Current streak with top partner | `15` |
| `%hypeup_top_max%` | Maximum streak with top partner | `20` |
| `%hypeup_top_color%` | Fire color of top streak | `&#FFD700` |
| `%hypeup_top_display%` | Fire display name of top streak | `🔥 Golden Fire` |
| `%hypeup_top_description%` | Fire description of top streak | `Blazing hot!` |
| `%hypeup_restore_count%` | Restore count used with top partner | `1` |

### Partner-Specific Placeholders
Replace `{N}` with partner number (1, 2, 3, etc.):

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%hypeup_partner_{N}_name%` | Name of Nth partner | `Alex` |
| `%hypeup_partner_{N}_streak%` | Current streak with Nth partner | `7` |
| `%hypeup_partner_{N}_max%` | Maximum streak with Nth partner | `10` |
| `%hypeup_partner_{N}_color%` | Fire color with Nth partner | `&#FF6B6B` |
| `%hypeup_partner_{N}_display%` | Fire display with Nth partner | `🔥 Red Fire` |
| `%hypeup_partner_{N}_expired%` | Whether fire is expired | `true/false` |

**Example Usage:**
- `%hypeup_partner_1_name%` - First partner's name
- `%hypeup_partner_2_streak%` - Second partner's streak
- `%hypeup_partner_3_color%` - Third partner's fire color

## Requirements

- **Minecraft:** 1.21+ (Paper/Spigot)
- **Java:** 21+
- **Dependencies:** PlaceholderAPI (Optional)

---

Made with ❤️ by MasterN
