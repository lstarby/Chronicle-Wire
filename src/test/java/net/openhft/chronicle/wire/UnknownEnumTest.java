package net.openhft.chronicle.wire;

import net.openhft.chronicle.bytes.Bytes;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UnknownEnumTest extends WireTestCommon {

    private static final byte[] SERIALISED_MAP_DATA = new byte[]{
            (byte) -59, 101, 118, 101, 110, 116, -126, 60, 0, 0, 0, -71, 3,
            107, 101, 121, -74, 47, 110, 101, 116, 46, 111, 112, 101, 110, 104,
            102, 116, 46, 99, 104, 114, 111, 110, 105, 99, 108, 101, 46, 119,
            105, 114, 101, 46, 85, 110, 107, 110, 111, 119, 110, 69, 110, 117,
            109, 84, 101, 115, 116, 36, 84, 101, 109, 112, -27, 70, 73, 82, 83, 84};

    public Wire createWire() {
        return new TextWire(Bytes.allocateElasticOnHeap(128));
    }

    @Test
    public void testUnknownEnum() {
        Wire wire = createWire();
        wire.write("value").text("Maybe");

        YesNo yesNo = wire.read("value").asEnum(YesNo.class);

        Wire wire2 = createWire();
        wire2.write("value").asEnum(yesNo);

        String maybe = wire2.read("value").text();
        assertEquals("Maybe", maybe);
    }

   // private enum Temp {
       // FIRST
   // }

    /*
    Documents the behaviour of BinaryWire when an enum type is unknown
     */
    @Test
    public void shouldConvertEnumValueToStringWhenTypeIsNotKnownInBinaryWire() throws Exception {

        // generates the serialised form
       // final Bytes<ByteBuffer> b = Bytes.allocateElasticOnHeap(128);
       // final Wire w = WireType.BINARY.apply(b);
//
       // final Map<String, Temp> m = new HashMap<>();
       // m.put("key", Temp.FIRST);
       // w.write("event").marshallable(m);
       // final ByteBuffer hb = b.underlyingObject();
       // hb.limit((int) b.writePosition());
       // while (hb.remaining() != 0) {
           // System.out.print(" " + hb.get() + ",");
       // }
       // System.out.println();

        final Bytes<ByteBuffer> bytes = Bytes.wrapForRead(ByteBuffer.wrap(SERIALISED_MAP_DATA));

        final Wire wire = WireType.BINARY.apply(bytes);
        final Map<String, Object> enumField = wire.read("event").marshallableAsMap(String.class, Object.class);
        assertEquals("FIRST", enumField.get("key"));
    }

    @Test
    public void shouldGenerateFriendlyErrorMessageWhenTypeIsNotKnownInTextWire() {
        Wires.GENERATE_TUPLES = true;
        try {
            final TextWire textWire = TextWire.from("enumField: !UnknownEnum QUX");
            textWire.valueIn.wireIn().read("enumField").object();

            fail();
        } catch (Exception e) {
            String message = e.getMessage().replaceAll(" [a-z0-9.]+.Proxy\\d+", " ProxyXX");
            assertThat(message,
                    is(equalTo("Trying to read marshallable class ProxyXX at [pos: 23, rlim: 27, wlim: 27, cap: 27 ]  QUX expected to find a {")));
        } finally {
            Wires.GENERATE_TUPLES = false;
        }
    }

    enum YesNo implements DynamicEnum {
        Yes,
        No
    }
}
