import banhFlanImage from "~/assets/images/menu/banh-flan.jpg";
import banhMiChaoImage from "~/assets/images/menu/banh-mi-chao.jpg";
import banhCuonNongImage from "~/assets/images/menu/banh-cuon-nong.jpg";
import banhSuKemImage from "~/assets/images/menu/banh-su-kem.jpg";
import bunBoHueImage from "~/assets/images/menu/bun-bo-hue.jpg";
import bunChaHaNoiImage from "~/assets/images/menu/bun-cha-ha-noi.jpg";
import bunRieuCuaImage from "~/assets/images/menu/bun-rieu-cua.jpg";
import caPheSuaDaImage from "~/assets/images/menu/ca-phe-sua-da.jpg";
import caPheDenDaImage from "~/assets/images/menu/ca-phe-den-da.png";
import cacaoNongImage from "~/assets/images/menu/cacao-nong.jpg";
import chaGioHaiSanImage from "~/assets/images/menu/cha-gio-hai-san.jpg";
import cheKhucBachImage from "~/assets/images/menu/che-khuc-bach.jpg";
import cheThapCamImage from "~/assets/images/menu/che-thap-cam.jpg";
import comGaXoiMoImage from "~/assets/images/menu/com-ga-xoi-mo.jpg";
import comTamSuonBiChaImage from "~/assets/images/menu/com-tam-suon-bi-cha.jpg";
import goiCuonTomThitImage from "~/assets/images/menu/goi-cuon-tom-thit.jpg";
import kemSauRiengImage from "~/assets/images/menu/kem-sau-rieng.jpg";
import lauThaiHaiSanImage from "~/assets/images/menu/lau-thai-hai-san.jpg";
import matchaLatteImage from "~/assets/images/menu/matcha-latte.jpg";
import nuocDuaTuoiImage from "~/assets/images/menu/nuoc-dua-tuoi.jpg";
import nuocEpCamImage from "~/assets/images/menu/nuoc-ep-cam.jpeg";
import nuocEpDuaImage from "~/assets/images/menu/nuoc-ep-dua.jpg";
import nuocEpOiImage from "~/assets/images/menu/nuoc-ep-oi.jpg";
import phoBoTaiImage from "~/assets/images/menu/pho-bo-tai.png";
import sinhToBoImage from "~/assets/images/menu/sinh-to-bo.jpg";
import sinhToXoaiImage from "~/assets/images/menu/sinh-to-xoai.jpg";
import sodaBacHaImage from "~/assets/images/menu/soda-bac-ha.jpg";
import suaDauNanhImage from "~/assets/images/menu/sua-dau-nanh.jpg";
import traChanhImage from "~/assets/images/menu/tra-chanh.jpg";
import traDaoHatChiaImage from "~/assets/images/menu/tra-dao-hat-chia.jpg";
import traGungMatOngImage from "~/assets/images/menu/tra-gung-mat-ong.jpg";
import traSuaTranChauImage from "~/assets/images/menu/tra-sua-tran-chau.jpg";
import traTacXiMuoiImage from "~/assets/images/menu/tra-tac-xi-muoi.png";
import traVaiImage from "~/assets/images/menu/tra-vai.jpg";
import yaourtDaImage from "~/assets/images/menu/yaourt-da.jpg";

type FoodImageRule = {
  patterns: string[];
  path: string;
};

const FOOD_IMAGE_RULES: FoodImageRule[] = [
  {
    patterns: ["tra sua", "trà sữa", "tran chau", "trân châu", "bubble tea", "boba"],
    path: traSuaTranChauImage,
  },
  {
    patterns: ["ca phe sua da", "cà phê sữa đá", "coffee sua da", "vietnamese iced coffee"],
    path: caPheSuaDaImage,
  },
  {
    patterns: ["ca phe den da", "cà phê đen đá", "ca phe den", "cà phê đen"],
    path: caPheDenDaImage,
  },
  {
    patterns: ["cacao nong", "cacao nóng", "ca cao nong", "ca cao nóng", "hot chocolate"],
    path: cacaoNongImage,
  },
  {
    patterns: ["nuoc ep dua", "nước ép dứa", "pineapple juice"],
    path: nuocEpDuaImage,
  },
  {
    patterns: ["nuoc ep oi", "nước ép ổi", "guava juice"],
    path: nuocEpOiImage,
  },
  {
    patterns: ["nuoc dua tuoi", "nước dừa tươi", "coconut water"],
    path: nuocDuaTuoiImage,
  },
  {
    patterns: ["soda bac ha", "soda bạc hà", "mint soda"],
    path: sodaBacHaImage,
  },
  {
    patterns: ["sua dau nanh", "sữa đậu nành", "soy milk"],
    path: suaDauNanhImage,
  },
  {
    patterns: ["tra chanh", "trà chanh", "lemon tea"],
    path: traChanhImage,
  },
  {
    patterns: ["tra dao hat chia", "trà đào hạt chia", "tra dao", "trà đào"],
    path: traDaoHatChiaImage,
  },
  {
    patterns: ["tra gung mat ong", "trà gừng mật ong", "tra hoa cuc mat ong", "trà hoa cúc mật ong", "ginger honey tea"],
    path: traGungMatOngImage,
  },
  {
    patterns: ["tra sen nhan nhuc", "trà sen nhãn nhục", "tra sen", "trà sen"],
    path: traVaiImage,
  },
  {
    patterns: ["tra vai", "trà vải", "lychee tea"],
    path: traVaiImage,
  },
  {
    patterns: ["yaourt da", "yaourt đá", "yogurt drink"],
    path: yaourtDaImage,
  },
  {
    patterns: ["sinh to xoai", "sinh tố xoài", "mango smoothie"],
    path: sinhToXoaiImage,
  },
  {
    patterns: ["com ga xoi mo", "cơm gà xối mỡ", "fried chicken rice"],
    path: comGaXoiMoImage,
  },
  {
    patterns: ["bun bo hue", "bún bò huế"],
    path: bunBoHueImage,
  },
  {
    patterns: ["bun cha ha noi", "bún chả hà nội", "bun cha", "bún chả"],
    path: bunChaHaNoiImage,
  },
  {
    patterns: ["bun rieu cua", "bún riêu cua", "bun rieu", "bún riêu"],
    path: bunRieuCuaImage,
  },
  {
    patterns: ["pho bo", "phở bò", "pho bo tai", "phở bò tái"],
    path: phoBoTaiImage,
  },
  {
    patterns: ["banh cuon nong", "bánh cuốn nóng", "banh cuon", "bánh cuốn"],
    path: banhCuonNongImage,
  },
  {
    patterns: ["com suon nuong mat ong", "cơm sườn nướng mật ong", "com suon", "cơm sườn"],
    path: comTamSuonBiChaImage,
  },
  {
    patterns: ["che khuc bach", "chè khúc bạch"],
    path: cheKhucBachImage,
  },
  {
    patterns: ["che thap cam", "chè thập cẩm"],
    path: cheThapCamImage,
  },
  {
    patterns: ["kem sau rieng", "kem sầu riêng", "durian ice cream"],
    path: kemSauRiengImage,
  },
  {
    patterns: ["banh su kem", "bánh su kem", "cream puff"],
    path: banhSuKemImage,
  },
  {
    patterns: ["banh flan", "bánh flan", "flan"],
    path: banhFlanImage,
  },
  {
    patterns: ["goi cuon tom thit", "gỏi cuốn tôm thịt", "goi cuon", "gỏi cuốn"],
    path: goiCuonTomThitImage,
  },
  {
    patterns: ["cha gio hai san", "chả giò hải sản", "cha gio", "chả giò"],
    path: chaGioHaiSanImage,
  },
  {
    patterns: ["salad bo luc lac", "salad bò lúc lắc", "salad bo", "salad bò"],
    path: goiCuonTomThitImage,
  },
  {
    patterns: ["khoai tay chien pho mai", "khoai tây chiên phô mai", "khoai tay chien", "khoai tây chiên"],
    path: chaGioHaiSanImage,
  },
  {
    patterns: ["com tam suon bi cha", "cơm tấm sườn bì chả", "com tam", "cơm tấm"],
    path: comTamSuonBiChaImage,
  },
  {
    patterns: ["mi xao bo", "mì xào bò", "mi xao", "mì xào"],
    path: comTamSuonBiChaImage,
  },
  {
    patterns: ["lau thai hai san", "lẩu thái hải sản", "lau thai", "lẩu thái"],
    path: lauThaiHaiSanImage,
  },
  {
    patterns: ["banh mi chao", "bánh mì chảo"],
    path: banhMiChaoImage,
  },
  {
    patterns: ["tra tac xi muoi", "trà tắc xí muội", "tra tac", "trà tắc"],
    path: traTacXiMuoiImage,
  },
  {
    patterns: ["nuoc ep cam", "nước ép cam", "cam tuoi", "cam tươi"],
    path: nuocEpCamImage,
  },
  {
    patterns: ["matcha latte", "matcha"],
    path: matchaLatteImage,
  },
  {
    patterns: ["sinh to bo", "sinh tố bơ", "avocado smoothie"],
    path: sinhToBoImage,
  },
  {
    patterns: ["combo com ga", "combo cơm gà"],
    path: comGaXoiMoImage,
  },
  {
    patterns: ["combo pho bo", "combo phở bò"],
    path: phoBoTaiImage,
  },
  {
    patterns: ["combo khai vi", "combo khai vị"],
    path: chaGioHaiSanImage,
  },
  {
    patterns: ["combo gia dinh", "combo gia đình"],
    path: lauThaiHaiSanImage,
  },
  {
    patterns: ["ba chi bo nuong", "bò nướng", "bo nuong", "com bo nuong"],
    path: comTamSuonBiChaImage,
  },
  {
    patterns: ["suon heo nuong", "sườn heo nướng", "mat ong", "mật ong"],
    path: comTamSuonBiChaImage,
  },
  {
    patterns: ["ga nuong", "gà nướng"],
    path: comGaXoiMoImage,
  },
  {
    patterns: ["hai san nuong", "hải sản nướng", "bach tuoc", "bạch tuộc", "mi cay hai san", "mì cay hải sản"],
    path: lauThaiHaiSanImage,
  },
  {
    patterns: ["dau hu nuong", "đậu hũ nướng", "lau nam chay", "lẩu nấm chay"],
    path: goiCuonTomThitImage,
  },
  {
    patterns: ["lau bo", "lẩu bò", "lau ga", "lẩu gà", "lau kim chi", "lẩu kim chi", "lau cua", "lẩu cua", "combo lau", "combo lẩu"],
    path: lauThaiHaiSanImage,
  },
  {
    patterns: ["bia", "cola", "nuoc suoi", "nước suối", "nuoc ngot", "nước ngọt"],
    path: traTacXiMuoiImage,
  },
  {
    patterns: ["nuoc chanh sa", "nước chanh sả", "chanh sa", "chanh sả"],
    path: traChanhImage,
  },
  {
    patterns: ["combo nuong", "combo nướng"],
    path: comTamSuonBiChaImage,
  },
];

const CATEGORY_IMAGE_PATHS: Record<string, string> = {
  "1": traChanhImage,
  "2": phoBoTaiImage,
  "3": banhFlanImage,
  "67": goiCuonTomThitImage,
  "68": lauThaiHaiSanImage,
};

const GENERIC_FOOD_IMAGE_PATH = phoBoTaiImage;

export const normalizeFoodText = (value: unknown) =>
  String(value ?? "")
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d");

export const isBrokenMenuImageUrl = (url?: string) => {
  if (!url) return true;
  const lower = url.toLowerCase();
  return (
    lower.includes("example.com") ||
    lower.includes("placeholder") ||
    lower.endsWith("/null") ||
    lower.endsWith("/undefined")
  );
};

export const getFoodImagePath = (
  name?: string,
  description?: string,
  categoryId?: string | number
) => {
  const haystack = normalizeFoodText(`${name || ""} ${description || ""}`);
  const matched = FOOD_IMAGE_RULES.find((item) =>
    item.patterns.some((pattern) => haystack.includes(normalizeFoodText(pattern)))
  );

  if (matched) return matched.path;
  return CATEGORY_IMAGE_PATHS[String(categoryId ?? "")] || GENERIC_FOOD_IMAGE_PATH;
};

export const getFoodFallbackImageUrl = (
  name?: string,
  description?: string,
  categoryId?: string | number
) => getFoodImagePath(name, description, categoryId);

export const getMenuImageUrl = (
  imageUrl?: string,
  name?: string,
  description?: string,
  categoryId?: string | number
) => {
  const localImagePath = getFoodImagePath(name, description, categoryId);
  void imageUrl;
  return localImagePath;
};
