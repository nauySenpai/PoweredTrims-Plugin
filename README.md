# 🛡️ PoweredTrims

**Transform your Minecraft armor trims into powerful gameplay elements!**

PoweredTrims is a comprehensive Spigot plugin that adds **custom attributes, effects, and set bonuses** to Minecraft's armor trim system. Turn decorative trims into meaningful gameplay choices with **198 unique combinations**, each with its own **powerful set bonus**.

---

## ✨ Features

### 🎯 **Core Features**
- **198 Trim Combinations** - All 18 patterns × 11 materials fully configured
- **Individual Effects** - Each armor piece provides unique bonuses
- **Set Bonus System** - Powerful bonuses when wearing full matching trim sets
- **Custom Attributes** - Armor, attack speed, movement speed, luck, and more
- **Potion Effects** - Permanent effects like Speed, Resistance, Water Breathing

### 🎨 **Customization**
- **Modular Configuration** - Individual files for each trim combination
- **Custom Lore Templates** - 18+ themed lore formats
- **Flexible Set Bonuses** - Mix materials within same pattern
- **Easy Management** - Organized file structure for easy editing

### 🔧 **Technical Features**
- **Permission System** - Granular command permissions
- **Hot Reload** - Update configs without server restart
- **Debug Tools** - Comprehensive debugging and statistics
- **Performance Optimized** - Efficient event handling and caching

---

## 🚀 Quick Start

### 📦 Installation
1. Download the latest `PoweredTrims-1.0.0.jar`
2. Place in your server's `plugins/` folder
3. Restart your server
4. Plugin auto-generates all configuration files

### ⚡ Instant Setup
- **Zero Configuration Required** - Works out of the box
- **198 Pre-configured Trims** - All combinations ready to use
- **198 Set Bonuses** - Every single combination has its own set bonus
- **Professional Lore** - Beautiful, themed item descriptions

---

## 🎮 Gameplay

### 🔹 **Individual Trim Effects**
Each armor piece with a trim provides:
- **Attribute Bonuses** - Armor, attack speed, movement speed, etc.
- **Potion Effects** - Speed, Resistance, Water Breathing, etc.

### 🎯 **Set Bonus System**
Wear 4 pieces of armor with the **same trim pattern** to unlock:
- **Powerful Set Effects** - Enhanced versions of individual effects
- **Unique Abilities** - Pattern-specific bonuses
- **Material Flexibility** - Mix any materials within same pattern
- **Progressive Power** - Copper → Iron → Gold → Diamond → Netherite progression

### 🎨 **Trim Themes**

| Pattern | Theme | Individual Effects | Set Bonus |
|---------|-------|-------------------|-----------|
| ⚡ **Bolt** | Lightning/Speed | Speed, Attack Speed | Lightning Storm |
| 🏰 **Sentry** | Guardian/Defense | Armor, Resistance | Guardian's Fortress |
| 👻 **Vex** | Phantom/Flying | Slow Falling, Jump Boost | Phantom's Flight |
| 🌊 **Coast** | Water/Ocean | Water Breathing, Dolphins Grace | Ocean's Mastery |
| 🌿 **Wild** | Nature/Beast | Strength, Jump Boost | Beast Master |
| 👁️ **Eye** | Vision/Utility | Night Vision, Luck | Oracle's Sight |
| 🔮 **Spire** | Magic/Arcane | Luck, Regeneration | Arcane Mastery |
| 🤫 **Silence** | Stealth/Shadow | Invisibility, Speed | Shadow Master |

*...and 10 more unique patterns!*

---

## ⚙️ Configuration

### 📁 **File Structure**
```
plugins/PoweredTrims/
├── config.yml              # Main settings & messages
├── lore-format.yml         # Lore templates
└── trims/                  # Individual trim configs
    ├── sentry/
    │   ├── gold.yml        # Sentry + Gold combination
    │   ├── diamond.yml     # Sentry + Diamond (with set bonus)
    │   └── netherite.yml   # Sentry + Netherite (ultimate)
    ├── bolt/
    │   ├── copper.yml      # Bolt + Copper combination
    │   └── ...
    └── ... (18 patterns total)
```

### 🎯 **Example Trim Configuration**
```yaml
# trims/sentry/gold.yml
enabled: true

attributes:
  GENERIC_ARMOR: 3.2
  GENERIC_ARMOR_TOUGHNESS: 1.2
  GENERIC_LUCK: 2.0

effects:
  - type: RESISTANCE
    amplifier: 0
  - type: ABSORPTION
    amplifier: 0

set_bonus:
  enabled: true
  name: "&6&lGuardian's Fortress"
  description: "&7Full protection mastery"
  requirements:
    pieces_needed: 4
    same_pattern: true
    same_material: false
  effects:
    - type: RESISTANCE
      amplifier: 1
    - type: REGENERATION
      amplifier: 0
  attributes:
    GENERIC_ARMOR: 2.0
    GENERIC_ARMOR_TOUGHNESS: 1.0

lore:
  enabled: true
  template: "defense"
```

---

## 🎛️ Commands & Permissions

### 📋 **Commands**
| Command | Description | Permission |
|---------|-------------|------------|
| `/pt stats` | Show plugin statistics | `poweredtrims.view` |
| `/pt reload` | Reload all configurations | `poweredtrims.admin` |
| `/pt debug` | Show debug information | `poweredtrims.admin` |
| `/pt help` | Display help message | None |

### 🔐 **Permissions**
- **`poweredtrims.view`** - Access to stats command
- **`poweredtrims.admin`** - All admin commands (reload, debug)

---

## 🔧 Advanced Features

### 🎨 **Custom Lore Templates**
18+ themed templates for different trim patterns:
- **Speed Template** - For Bolt trims (lightning theme)
- **Defense Template** - For Sentry trims (guardian theme)
- **Water Template** - For Coast trims (ocean theme)
- **Mobility Template** - For Vex trims (flying theme)
- *...and many more!*



### 🛠️ **Development Tools**
- **Hot Reload** - Update configs without restart
- **Debug Mode** - Detailed logging and troubleshooting
- **Modular Structure** - Easy to modify and extend

---

## 📊 Statistics

- **✅ 198 Trim Combinations** - All patterns × materials configured
- **✅ 198 Set Bonuses** - Every single combination has its own set bonus
- **✅ 18 Themed Templates** - Pattern-specific lore formats
- **✅ Zero Configuration** - Works immediately after installation
- **✅ Full Compatibility** - Supports all Minecraft versions with trims

---

## 🤝 Support & Community

### 📞 **Getting Help**
- **GitHub Issues** - Bug reports and feature requests
- **Discord Support** - Real-time community help
- **Wiki Documentation** - Comprehensive guides and tutorials

### 🔄 **Updates**
- **Regular Updates** - New features and improvements
- **Backward Compatibility** - Configs preserved across versions
- **Community Feedback** - Features driven by user requests

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Transform your server's armor system today with PoweredTrims!** 🚀
