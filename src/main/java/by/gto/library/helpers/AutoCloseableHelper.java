package by.gto.library.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс-помощник (обертка, фасад, Facade) для облегчения закрытия множества AutoCloseables, используя механизм try-with resources.
 * Не является потокобезопасным, предназначен для работы в одном потоке.
 * Ведет "список закрытия" ресурсов, которые должны быть закрыты при закрытии самой обертки. Закрытие происходит в порядке,
 * обратном порядку добавления ресурсов.
 *
 * Пример использования:
 * <pre>
 * try(AutoCloseableHelper ah = new AutoCloseableHelper()) {
 *   Connection c = ah.add(createConnection());
 *   ...
 *   Statement s = ah.add(c.createStatement())
 *   ..
 *   ResultSet rs = ah.add(s.execute());
 * }
 * </pre>
 * В этом случае при выходе из try/catch ресурсы будут закрыты в порядке, обратном порядку добавления: rs, s, c
 */
public final class AutoCloseableHelper implements AutoCloseable {
    private final List<AutoCloseable> closeables = new ArrayList<>();

    /**
     * Закрыть все добавленные ресурсы. Если при закрытии ресурса возникает исключение, оно проглатывается.
     */
    @Override
    public void close() {
        for (int i = closeables.size() - 1; i >= 0; i--) {
            try {
                closeables.get(i).close();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Добавить ресурс в список на автозакрытие. Если ресурс == null, он НЕ ДОБАВЛЯЕТСЯ в список.
     * Повторное добавление того же ресурса не отслеживается.
     * @param autoCloseable добавляемый ресурс
     * @return добавляемый ресурс (просто для удобства)
     */
    public <R extends AutoCloseable> R add(R autoCloseable) {
        if (autoCloseable != null) {
            closeables.add(autoCloseable);
        }
        return autoCloseable;
    }

    /**
     * Удалить ресурс из списка на автозакрытие. Опционально -  закрыть ресурс и если параметр close = true.
     * Если при закрытии ресурса возникает исключение, оно проглатывается.
     * Если заказано закрытие, то переданный ресурс закрывается вне зависимости от его присутствия во внутреннем списке закрытия.
     * @param autoCloseable удаляемый ресурс и опционально закрываемый ресурс.
     * @param close закрывать ресурс при удалении из списка.
     */
    public void remove(AutoCloseable autoCloseable, boolean close) {
        if (autoCloseable == null) {
            return;
        }
        for (int i = closeables.size() - 1; i >= 0; i--) {
            AutoCloseable el = closeables.get(i);
            if (el == autoCloseable) {
                closeables.remove(i);
            }
        }
        if(close) {
            try {
                autoCloseable.close();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * просто конструктор по умолчанию.
     */
    public AutoCloseableHelper() {
    }

    /**
     * Конструктор для инициализации списка объектов для автозакрытия.
     * @param autoCloseables - список объектов для автозакрытия.
     */
    public AutoCloseableHelper(AutoCloseable... autoCloseables) {
        for (AutoCloseable a : autoCloseables) {
            if (a != null) {
                closeables.add(a);
            }
        }
    }

    /**
     * Закрывает переданные объекты, проглатывая возникающие при этом исключения.
     * Просто для удобства и уменьшения объема шаблонного кода.
     * Присутствие закрываемого ресурса во внутреннем списке закрытия не проверяется.
     *
     * @param autoCloseables объекты для закрытия.
     */
    public static void closeWithoutExceptions(AutoCloseable... autoCloseables) {
        for (AutoCloseable autoCloseable : autoCloseables) {
            if (autoCloseable == null) {
                continue;
            }
            try {
                autoCloseable.close();
            } catch (Throwable ignored) {
            }
        }
    }
}
