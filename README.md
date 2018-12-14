3 Goals: Configurable, Compatible, Comprehensible
# Eventials
* Every command and feature comes with complete, carefully designed permissions
* Each section can be enabled/disabled in the config
* Carefully structured Time complexity (This plugin won't lag)
* Almost all displayed messages are configurable<br>

Although I plan to eventually do a public release of this plugin, there is currently too much legacy code and dependance on my other plugins for **Eventials** to be very useful to any other server in its current form.

### Economy
* Non-infinite server balance `/serverbal [add/take/transfer]` | Config: starting balance, min balance, max balance
* Server balance limit | Config: result when transaction < min or > max, message
* View sum of players & server balance `/globalbal`
* Players donating ingame money to server `/donateserver <amount>`, `/donatetop`
* Item-as-currency `/deposit [amount]`, `/withdraw [amount]` | Config: item, deposit/withdraw tax rate, symbol
* Currency symbol, name, and transaction-related messages
* Money orders `/moneyorder [MIN <= amount <= MAX]` | Config: min, max, default, and tax rate, message
* Advertise in MOTD `/advertise <Player's advertisement>` | Config: cost, duration, max/max-length
* Balance lookup `/bal [name]`, `/baltop [name/page#]`
* Reward for Advancements | Config: message, function(num advanements)->amount
* Reward for logging in daily | Config: message, function(streak)->amount
* Sign-based AdminShop
* Charge for generating chunks | Config: cost, message
* Cost for running certain commands | Config: chosen commands and associated costs
* Direct player costs/fees/purchases into the server's balance
* Use server balance to pay players (e.g. vote reward, daily login, community service)


### Scheduler / Messenger
* Custom server ping message, playercount, and hover text
* Automessages broadcast in chat | Config: messages, frequency, skip-if-afk
* Automessages support click-to-run-command, hover-text, hyperlinks, and all the other good stuff
* Mob butcher | Config: frequency, mob attributes, server conditions (e.g. TPS)
* Backups | Config: frequency, worlds, plugin data
* Noteblock sound on player login
* Show recently joined players on login
* Date-specific server ping messages
* Date-specific scheduled events (e.g. free pumpkins Oct 31)


### Vote Rewards
* Voting `/vote` | Config: site link(s), site name(s), single-site
* Custom reward items | Config: fixed or randomized, list of items and probabilities
* Reward currency | Config: amount, paid from server balance yes/no
* Reward for voting daily | Config: message, function(streak)->reward
* Voting streaks | Config: grace period, streak recovery

### Spawners
* Mine and place Mob Spawners | Config: require silktouch
* Chance to spawn Mob Armies | Config: probability, size, standard deviation
* Feed slimes slimeblocks to increase their size (scales with size)
* Adjust skeleton trap probability
* Custom mob-butcher `/butcher [worldname] [flag1 flag2 ...] (a=animals c=complex n=named h=hostile e=nonliving f=compound)`
* Disable nearby block-physics updates `/nophysics [name]` (e.g. for creating floating torches, water, etc.)

### Miscellaneous
* Custom crafting recipes; default: Horse armor, saddles, nametags, double-slabs
* Command cooldowns | Config: chosen commands and cooldown times
* Custom command aliases; default: `/h` for `/home`, `/b` for `/back`, etc.
* Upgraded `/help [command/plugin/page#]` menu
* Clickable `/warps` and `/homes`
* IP blacklist (server will show as "Unknown Host")
* Disable MultiCraft hosting spam (if applicable)
* Clear old player data | Config: inactivity, advancements, whitelist, ops, balance, time online
* Clear unused world chunks | Config: inactivity, creation date, edited %, filesize, version, distance from 0,0
* Tell a player his/her coordinates on death and log to console
* Custom `/plugins` with extra data on hover | Config: shown plugins, plugin descriptions
* Per-world World Border | Config: center, radius
* Enable color codes `§` in command blocks
* Configure text on signs in the chat bar
* For a price, show statistics on a weapon or tool (e.g. # of mobs killed) | Config: price, items, available stats
* Commands for giving/revoking player donator status
* Delete/restore all server data pertaining to a player
* Edit itemlore and name using color codes
* Check current region (.mca file) or teleport to a specified region
* Identify and clean up unused chunk or player data for a world save
* View an orderd list of players most recently online
* `/pig [name]` food bar will never go above 19 points, enabling endless glutony
* View detailed information about a player `/insight <name/uuid>`
* Become transparent but not invisible `/ghost [name]`
* Place floating text `/ftxt <place/remove/list/edit/move/help>`
* Option to hide player login/quit messages
* Disguise a player as a mob (currently broken)

### Particles
* 25+ custom particle effects, here are some highlights:
* Texas Flag: displays a giant Texas flag coming out of your head
* Rainbow: two clouds above either shoulder with a rainbow overhead
* Raincloud: Storm cloud overhead with dripping water particles
* Blizzard: Snowy particles blowing all around player obstructing vision
* Lazer: lazer beam protruding from your chest
* Admin: the word "Admin" writen in big, fiery letters over your head
* Imagine: random noteblock particles in surrounding area
* Nova: Random geometric shapes drawn in fine dust around the player
* Batwings: Large black wings (visual only) sprouting from your shoulder tops

### Bows
* A suite of custom-made bows (given as Event prizes at one point)
* All bows have the Unbreakable tag and thus won't lose durability
* Gandiva: +2 attack, +25% kb resist, -5% speed, Silktouch, Flame 2, Power 8
* Flint: +2 attack, +5% speed, fires at velocity x2
* Ichaival: Fires up to 9 arrows at once, but can't use Infinity
* Determined: Can be used to fire arrows, epearls, snowballs, eggs, fireballs, splash potions, xp bottles, spawn eggs, or withe skulls
* Traveler: fires non-gravity arrows (travel 250 blocks max), glowing arrows, +100% kb, +25% velocity 

### Music
* Upload an mp3 file into Noteblock music
* Place Noteblock music ingame using sound packets
* Server public/private album manager
* Download Noteblock music as mp3
