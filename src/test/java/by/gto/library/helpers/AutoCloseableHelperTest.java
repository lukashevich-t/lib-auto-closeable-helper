package by.gto.library.helpers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class AutoCloseableHelperTest {
    @Test
    public void testAutoCloseableHelper() {
        {
            MyCloseable.nextId = 1;
            List<Integer> openCloseOrder = new ArrayList<>(10);

            try (AutoCloseableHelper ach = new AutoCloseableHelper()) {
                MyCloseable c1 = ach.add(new MyCloseable(openCloseOrder));
                MyCloseable c2 = ach.add(new MyCloseable(openCloseOrder));
                ach.closeAndForget(c1);
            } catch (Exception ignored) {
            }
            Assert.assertEquals(Arrays.asList(1, 2, -1, -2), openCloseOrder);
        }

        {
            MyCloseable.nextId = 1;
            List<Integer> openCloseOrder = new ArrayList<>(10);

            try (AutoCloseableHelper ach = new AutoCloseableHelper()) {
                MyCloseable c1 = ach.add(new MyCloseable(openCloseOrder));
                MyCloseable c2 = ach.add(new MyCloseable(openCloseOrder));
            } catch (Exception ignored) {
            }
            Assert.assertEquals(Arrays.asList(1, 2, -2, -1), openCloseOrder);
        }

        {
            MyCloseable.nextId = 1;
            List<Integer> openCloseOrder = new ArrayList<>(10);

            try (AutoCloseableHelper ach = new AutoCloseableHelper()) {
                MyCloseable c1 = ach.add(new MyCloseable(openCloseOrder));
                MyCloseable c2 = ach.add(new MyCloseable(openCloseOrder));
                for (int i = 0; ; i++) {
                    MyCloseable c = ach.add(new MyCloseable(openCloseOrder));
                    if (i >= 1) {
                        throw new ParseException("1", 1);
                    }
                    ach.closeAndForget(c);
                }
            } catch (Exception ignored) {
            }
            Assert.assertEquals(Arrays.asList(1, 2, 3, -3, 4, -4, -2, -1), openCloseOrder);
        }
    }

    static class MyCloseable implements AutoCloseable {
        private final List<Integer> openCloseOrder;
        private final int id;
        public static int nextId = 1;

        public MyCloseable(List<Integer> openCloseOrder) {
            this.id = nextId++;
            this.openCloseOrder = openCloseOrder;
            openCloseOrder.add(this.id);
        }

        @Override
        public void close() throws Exception {
            openCloseOrder.add(-id);
        }
    }
}
