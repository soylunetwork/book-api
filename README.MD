# Book-API

A high-performance, version-agnostic library for creating rich, interactive books on **Spigot / Paper** servers (1.8 – 1.21.8).

## Highlights

- One jar, **all versions** – no legacy modules needed  
- Type-safe, **fluent builder** with MiniMessage & legacy color support  
- **Zero-allocation** hot-path – no reflection after bootstrap  
- Fail-fast design – every error is a meaningful `BookApiException`  
- **JitPack** ready – single dependency, shaded gson included

---

## Quick Start

```java
public final class MyPlugin extends JavaPlugin {

    private BookAPI api;

    @Override
    public void onEnable() {
        api = BookAPI.create(this);               // one line bootstrap
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        Book book = BookAPI.miniMessageBuilder()
                .title("Tutorial")
                .author("Server")
                .addClickablePage("§6§lWarp Spawn", "warp spawn")
                .addHoverPage("§7Need §f§lhelp§7?", "Click to open the help menu")
                .build();

        api.openBook(p, book);                    // opens instantly
        return true;
    }
}
```

---

## API Cheatsheet

| Task | Code |
|---|---|
| Simple book | `BookAPI.builder().title("Hi").addPage("Hello").build()` |
| MiniMessage | `BookAPI.miniMessageBuilder().addMiniMessagePage("<rainbow>Magic</rainbow>").build()` |
| Menu page | `builder.addMenuPage("Choose", "Spawn:/spawn", "Shop:/shop")` |
| Convert ItemStack | `api.fromItemStack(item)` |
| Check is book | `Book.isItemStackBook(item)` |

---

## Building

```bash
./gradlew shadowJar
# output: build/libs/book-api-1.0.1-all.jar
```

---

## License

MIT – see [LICENSE](LICENSE) file.

**Author:** sheduxdev (soylunetwork)