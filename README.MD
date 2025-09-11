# Book API

Minecraft 1.21.4 ve üstü sürümler için geliştirilmiş, NMS yansıtma (reflection) kullanarak oyunculara kitap oluşturma, düzenleme ve görüntüleme işlemlerini kolaylaştıran kapsamlı bir kütüphane.

[![Yapılandırma Durumu](https://github.com/soylunetwork/book-api/workflows/Yap%C4%B1land%C4%B1rma%20ve%20Yay%C4%B1n/badge.svg)](https://github.com/soylunetwork/book-api/actions)
[![Lisans: MIT](https://img.shields.io/badge/Lisans-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Sürümü](https://img.shields.io/badge/Java-21+-blue.svg)](https://openjdk.java.net/)
[![Minecraft Sürümü](https://img.shields.io/badge/Minecraft-1.21.4+-green.svg)](https://www.minecraft.net/)

## Özellikler

- 📖 **Kolay Kitap Oluşturma** - Akıcı (fluent) builder API ile hızlı ve pratik kitap oluşturma
- 🎨 **Zengin Metin Desteği** - Renk kodları, biçimlendirme ve hexadecimal renk desteği
- 🔄 **Format Dönüşümleri** - Eski formatlar, JSON bileşenleri ve düz metin arasında dönüşüm
- 📄 **Akıllı Sayfalama** - Metni otomatik bölme ve sayfa yönetimi
- 🔧 **NMS Yansıtma** - Sürüm bağımsız NMS erişimiyle yüksek uyumluluk
- 📚 **Çoklu Format Desteği** - İmzalı kitaplar, yazılabilir kitaplar ve ItemStack dönüşümleri
- 🛠️ **Yardımcı Araçlar** - Metin sarma, doğrulama ve biçimlendirme yardımcıları
- 🚀 **Yüksek Performans** - Optimize edilmiş önbellekleme ve yansıtma işlemleri
- 📖 **Kapsamlı Dokümantasyon** - Tam JavaDoc ve ayrıntılı örnek kodlar
- 🌈 **MiniMessage Desteği** - MiniMessage formatıyla zengin metin oluşturma, tıklanabilir komutlar, üzerine gelindiğinde görünen metinler, gradyan ve gökkuşağı efektleri

## Kurulum

### Gradle
```gradle
repositories {
    maven { url 'https://maven.pkg.github.com/soylunetwork/book-api' }
}

dependencies {
    implementation 'network.soylu.book_api:book-api:1.0.0'
}
```

### Maven
```xml
<repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/soylunetwork/book-api</url>
</repository>

<dependency>
    <groupId>network.soylu.book_api</groupId>
    <artifactId>book-api</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Manuel Kurulum
1. En son JAR dosyasını [Releases](https://github.com/soylunetwork/book-api/releases) sayfasından indirin.
2. JAR dosyasını projenizin bağımlılıklarına ekleyin.
3. Gerekirse kütüphaneyi eklentinizin içine gölgelendirin (shade).

## Hızlı Başlangıç

### 1. API’yi Başlatma
```java
public class Eklentiniz extends JavaPlugin {
    @Override
    public void onEnable() {
        // Book API’yi başlat - diğer yöntemleri kullanmadan önce çağrılmalıdır
        BookAPI.initialize(this);
        getLogger().info("Book API başlatıldı!");
    }
}
```

### 2. Basit Bir Kitap Oluşturma ve Açma
```java
// Book API örneğini al
BookAPI bookAPI = BookAPI.getInstance();

// Basit bir kitap oluştur
Book book = BookAPI.builder()
    .title("İlk Kitabım")
    .author("Eklenti Geliştiricisi")
    .addPage("Merhaba, dünya!")
    .addPage("Bu ikinci sayfa")
    .signed()
    .build();

// Kitabı bir oyuncu için aç
bookAPI.openBook(player, book);
```

### 3. Gelişmiş Kitap Oluşturma
```java
Book gelismisKitap = BookAPI.builder()
    .title("§6Gelişmiş Kitap")
    .author("§bBook API")
    .addPage("§aHoş geldiniz!\n\nBu kitap gelişmiş özellikleri tanıtır.")
    .addWrappedPage("Bu çok uzun bir metin. Kitap sayfalarına sığacak şekilde otomatik olarak sarılır ve görüntüleme sorunlarına yol açmaz.")
    .addText("Bu metin birden fazla sayfaya yayılır ve Minecraft’ın kitap sınırlamalarına uygun şekilde otomatik olarak bölünür.")
    .signed()
    .build();

bookAPI.openBook(player, gelismisKitap);
```

### 4. MiniMessage ile Kitap Oluşturma
```java
Book menuKitap = BookAPI.miniMessageBuilder()
    .title("Sunucu Menüsü")
    .author("Sistem")
    .addMenuPage("Sunucu Komutları",
        "Spawn’a Git:/spawn",
        "Evine Git:/home",
        "Yardım:/help")
    .addGradientPage("Gradyan Metin!", "#FF0000", "#0000FF")
    .addRainbowPage("Gökkuşağı Metni!")
    .build();

bookAPI.openBook(player, menuKitap);
```

## API Referansı

### BookBuilder Yöntemleri
| Yöntem | Açıklama |
|--------|----------|
| `title(String)` | Kitap başlığını belirler (maks. 32 karakter) |
| `author(String)` | Kitap yazarını belirler (maks. 16 karakter) |
| `addPage(String)` | Belirtilen içeriğe sahip bir sayfa ekler |
| `addPages(String...)` | Birden fazla sayfa ekler |
| `addWrappedPage(String)` | Otomatik metin sarma ile sayfa ekler |
| `addText(String)` | Gerekirse birden fazla sayfaya yayılan metin ekler |
| `insertPage(int, String)` | Belirtilen indekse bir sayfa ekler |
| `removePage(int)` | Belirtilen indeksteki sayfayı kaldırır |
| `clearPages()` | Tüm sayfaları kaldırır |
| `signed(boolean)` | Kitabın imzalanıp imzalanmayacağını belirler |
| `signed()` | Kitabı imzalı olarak işaretler |
| `withMiniMessage()` | MiniMessage formatını etkinleştirir |
| `addMenuPage(String, String...)` | Tıklanabilir komutlarla bir menü sayfası ekler |
| `addGradientPage(String, String, String)` | Gradyan renk efektli bir sayfa ekler |
| `addRainbowPage(String)` | Gökkuşağı efektli bir sayfa ekler |
| `addClickablePage(String, String)` | Tıklanabilir komut içeren bir sayfa ekler |
| `addHoverPage(String, String)` | Üzerine gelindiğinde metin gösteren bir sayfa ekler |

### BookAPI Yöntemleri
| Yöntem | Açıklama |
|--------|----------|
| `openBook(Player, Book)` | Belirtilen oyuncu için bir kitap açar |
| `openBook(Player, ItemStack)` | Oyuncu için bir ItemStack kitabını açar |
| `fromItemStack(ItemStack)` | ItemStack’ten Book nesnesine dönüştürür |
| `toItemStack(Book)` | Book nesnesini ItemStack’e dönüştürür |
| `isBook(ItemStack)` | ItemStack’in kitap olup olmadığını kontrol eder |
| `openMiniMessageBook(Player, String, String, String...)` | MiniMessage formatında bir kitap açar |
| `openMenuBook(Player, String, String, String...)` | Tıklanabilir komutlarla menü kitabı açar |
| `miniMessageToJson(String)` | MiniMessage metnini JSON formatına çevirir |
| `jsonToMiniMessage(String)` | JSON formatını MiniMessage’a çevirir |
| `legacyToMiniMessage(String)` | Eski formatı MiniMessage’a çevirir |

### Yardımcı Sınıflar

#### BookUtils
```java
// Metin doğrulama ve biçimlendirme
String dogrulananSayfa = BookUtils.validateAndTruncatePage(uzunMetin);
List<String> sayfalar = BookUtils.splitTextIntoPages(uzunMetin);
String sarilmisMetin = BookUtils.wrapText(metin, 19, 14);
boolean sigarMi = BookUtils.fitsInOnePage(metin);
String baslik = BookUtils.createPageHeader("Başlık");
String ortalanmisMetin = BookUtils.centerText("Merhaba");
```

#### ComponentSerializer
```java
ComponentSerializer serializer = bookAPI.getComponentSerializer();

// Formatlar arasında dönüşüm
String json = serializer.legacyToJson("§aYeşil metin");
String eskiFormat = serializer.jsonToLegacy(jsonBileseni);
String sadeMetin = serializer.stripColors("§cKırmızı metin");
```

#### MiniMessageSerializer
```java
MiniMessageSerializer serializer = bookAPI.getMiniMessageSerializer();

// MiniMessage ile dönüşüm ve biçimlendirme
String json = serializer.miniMessageToJson("<red>Kırmızı metin</red>");
String miniMessage = serializer.jsonToMiniMessage(jsonBileseni);
String tiklanabilir = serializer.createClickableCommand("Tıkla!", "/spawn");
String uzerineGelinir = serializer.createHoverText("Metin", "Bu bir ipucu!");
String gradyan = serializer.createGradientText("Gradyan!", "#FF0000", "#0000FF");
String gokkusagi = serializer.createRainbowText("Renkli metin!");
```

## Örnekler

### Hikaye Kitabı
```java
Book hikayeKitabi = BookAPI.builder()
    .title("Macera")
    .author("Hikaye Anlatıcısı")
    .addPage("Bölüm 1: Başlangıç\n\nBir varmış, bir yokmuş...")
    .addPage("Bölüm 2: Yolculuk\n\nKahramanımız yola çıktı...")
    .addPage("Bölüm 3: Son\n\nVe mutlu sona ulaştılar.")
    .signed()
    .build();
```

### Rehber Kitap
```java
BookBuilder rehberBuilder = BookAPI.builder()
    .title("Sunucu Rehberi")
    .author("Yönetici");

rehberBuilder.addPage("Sunucumuza hoş geldiniz!\n\nKurallar:\n1. Saygılı olun\n2. Yıkım yapmayın\n3. Eğlenin!");
rehberBuilder.addPage("Komutlar:\n/spawn - Spawn noktasına git\n/home - Eve git\n/help - Yardım al");
rehberBuilder.addPage("İletişim:\nDiscord: discord.gg/sunucu\nWeb: sunucu.com");

Book rehber = rehberBuilder.signed().build();
```

### Renkli Kitap
```java
Book renkliKitap = BookAPI.builder()
    .title("§6Renkli Kitap")
    .author("§bSanatçı")
    .addPage("§cKırmızı §9Mavi §aYeşil\n§eSarı §5Mor §fBeyaz")
    .addPage("§lKalın §oİtalik §nAltı Çizili\n§mÜstü Çizili §kKarışık")
    .addPage("§x§f§f§6§6§0§0Özel hex renkleri!\n§x§0§0§f§f§0§0Muhteşem gradyanlar!")
    .signed()
    .build();
```

### MiniMessage Menü Kitabı
```java
Book menuKitap = BookAPI.miniMessageBuilder()
    .title("Sunucu Menüsü")
    .author("Sistem")
    .addMenuPage("Sunucu Komutları",
        "Spawn’a Git:/spawn",
        "Evine Git:/home",
        "Yardım:/help")
    .addInteractivePage("Web Sitesi", "open_url", "https://sunucu.com", "Sitemizi ziyaret et!")
    .build();

bookAPI.openBook(player, menuKitap);
```

## Desteklenen Sürümler

| Minecraft Sürümü | Destek Durumu |
|------------------|---------------|
| 1.21.4+ | ✅ Tam Destek |
| 1.21.x | ✅ Destekleniyor |
| 1.20.x | ⚠️ Kısmi Destek |
| < 1.20 | ❌ Desteklenmiyor |

## Katkıda Bulunma

Katkılarınızı memnuniyetle karşılıyoruz! Ayrıntılar için [Katkı Rehberi](CONTRIBUTING.md) dosyasını inceleyin.

### Kaynaktan Derleme
```bash
git clone https://github.com/soylunetwork/book-api.git
cd book-api
./gradlew build
```

### Testleri Çalıştırma
```bash
./gradlew test
```

## Sorun Giderme

### Yaygın Sorunlar

**Kitap oyuncuya açılmıyor**
- `BookAPI.initialize()` metodunun eklentinizin `onEnable()` metodunda çağrıldığından emin olun.
- Oyuncunun çevrimiçi olduğunu ve aynı dünyada bulunduğunu kontrol edin.
- Sunucu sürümünün desteklendiğini doğrulayın.

**Başlangıçta yansıtma hataları**
- Minecraft sürüm uyumluluğunu kontrol edin.
- Java 21 veya üstü bir sürüm kullandığınızdan emin olun.
- Kütüphaneyi en son sürüme güncelleyin.

**Sayfalar düzgün görüntülenmiyor**
- Sayfaları 1024 karakterin altında tutun.
- Satır sonları için `\n` kullanın.
- Metni `BookUtils` yöntemleriyle doğrulayın.

**Renk kodları çalışmıyor**
- § sembolünü kullanın (Alt+21 veya örneklerden kopyalayın).
- Hex renkler için format: `§x§f§f§6§6§0§0`
- İstemcinizin renk formatlarını desteklediğinden emin olun.

**MiniMessage formatı çalışmıyor**
- `withMiniMessage()` metodunu çağırdığınızdan emin olun.
- MiniMessage sözdiziminin doğru olduğundan emin olun (ör. `<red>Metin</red>`).
- `MiniMessageSerializer` ile metni doğrulayın.

### Yardım Alma
- 🐛 Hataları [Sorunlar](https://github.com/soylunetwork/book-api/issues) sayfasında bildirin.
- 💬 Sorularınızı [Tartışmalar](https://github.com/soylunetwork/book-api/discussions) sayfasında paylaşın.

## Lisans

Bu proje MIT Lisansı altında lisanslanmıştır. Ayrıntılar için [LICENSE](LICENSE) dosyasını inceleyin.

## Teşekkürler

- [upperlevel/book-api](https://github.com/upperlevel/book-api) projesinden ilham alındı.
- Spigot ve Paper topluluklarına teşekkürler.
- Geri bildirimleriyle katkıda bulunan tüm kullanıcılara ve geliştiricilere teşekkürler.