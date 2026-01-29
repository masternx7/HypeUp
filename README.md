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
