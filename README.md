# 🔥 HypeUp - TikTok Style Fire Streak Plugin

A Minecraft plugin that brings TikTok's fire streak system to the game! Players can build relationships with friends through fire streaks and earn rewards for consecutive days.

## Commands

| Command | Description | Permission |
|--------|----------|------------|
| `/hypeup` | Open fire partner list GUI | `hypeup.use` |
| `/hypeup help` | Show help message | `hypeup.use` |
| `/hypeup send <player>` | Send items to player | `hypeup.use` |
| `/hypeup info [player]` | View fire streak information | `hypeup.use` |
| `/hypeup list` | View all fire partners | `hypeup.use` |
| `/hypeup reload` | Reload configuration | `hypeup.admin` |

## How to Light Fire

To maintain a fire streak with your partner, you must complete **3 daily missions** before midnight:

### Mission Requirements

1. **Chat Mission**
   - Send at least **2 messages** to your partner
   - Messages must be in chat (not commands)
   - Anti-spam protection prevents instant completion

2. **Shift Mission**
   - Press **Shift (Sneak)** near your partner at least **2 times**
   - Must be within configured distance (default: 3 blocks)
   - Shows progress in chat

3. **Gift Mission**
   - Use `/hypeup send <partner>` to open gift GUI
   - Place items in the GUI and confirm
   - Items will be transferred to partner's inventory

### Lighting the Fire

1. Choose a partner using `/hypeup send <player>`
2. Complete all 3 missions (Chat, Shift, Gift)
3. Fire will automatically light when all missions are done
4. Your streak increases by 1 day
5. **Important:** Must complete missions daily before midnight to maintain streak!

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
