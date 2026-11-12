package bt3.network;

import bt3.medicine.Medicine;
import bt3.usage.Usage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Validation {
    public static final int ID_MAX = 10;
    public static final int NAME_MAX = 100;
    public static final int DESCRIPTION_MAX = 255;
    public static final int FORM_MAX = 50;
    public static final long PRICE_MAX = 100_000_000L;

    private static final Pattern ID = Pattern.compile("[A-Z0-9_-]+");
    private static final Pattern DIGITS = Pattern.compile("\\d{1,9}");

    private Validation() {
    }

    public static String id(String raw, String what) throws RequestException {
        String id = Protocol.clean(raw).toUpperCase(Locale.ROOT);
        if (id.isEmpty()) {
            throw new RequestException("Chưa nhập mã " + what);
        }
        if (id.length() > ID_MAX || !ID.matcher(id).matches()) {
            throw new RequestException("Mã " + what + " chỉ gồm chữ không dấu, số, '-' hoặc '_', tối đa " + ID_MAX
                    + " ký tự");
        }
        return id;
    }

    public static List<String> ids(String raw, String what) throws RequestException {
        List<String> ids = new ArrayList<>();
        for (String part : Protocol.clean(raw).split(Protocol.ID_LIST)) {
            if (!part.isBlank() && !ids.contains(id(part, what))) {
                ids.add(id(part, what));
            }
        }
        if (ids.isEmpty()) {
            throw new RequestException("Chưa chọn " + what + " nào");
        }
        return ids;
    }

    public static String text(String raw, String field, int max, boolean required) throws RequestException {
        String text = Protocol.clean(raw);
        if (required && text.isEmpty()) {
            throw new RequestException("Chưa nhập " + field);
        }
        if (text.length() > max) {
            throw new RequestException(capitalize(field) + " dài tối đa " + max + " ký tự");
        }
        return text;
    }

    public static long price(String raw) throws RequestException {
        String digits = Protocol.clean(raw).replace(".", "").replace(",", "");
        if (digits.isEmpty()) {
            throw new RequestException("Chưa nhập giá thuốc");
        }
        if (!DIGITS.matcher(digits).matches() || Long.parseLong(digits) > PRICE_MAX) {
            throw new RequestException("Giá phải là số nguyên từ 0 đến "
                    + NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(PRICE_MAX) + " VNĐ");
        }
        return Long.parseLong(digits);
    }

    public static Medicine medicine(String id, String name, String price, String usageId, String form)
            throws RequestException {
        return Medicine.of(id(id, "thuốc"), text(name, "tên thuốc", NAME_MAX, true), price(price),
                id(usageId, "công dụng"), text(form, "dạng bào chế", FORM_MAX, true));
    }

    public static Usage usage(String id, String name, String description) throws RequestException {
        return Usage.of(id(id, "công dụng"), text(name, "tên công dụng", NAME_MAX, true),
                text(description, "mô tả", DESCRIPTION_MAX, false));
    }

    private static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }
}
