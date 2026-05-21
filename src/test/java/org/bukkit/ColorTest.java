package org.bukkit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("javadoc")
public class ColorTest {
    static class TestColor {
        static int id = 0;
        final String name;
        final int rgb;
        final int bgr;
        final int r;
        final int g;
        final int b;

        TestColor(int rgb, int bgr, int r, int g, int b) {
            this.rgb = rgb;
            this.bgr = bgr;
            this.r = r;
            this.g = g;
            this.b = b;
            this.name = id + ":" + Integer.toHexString(rgb).toUpperCase() + "_" + Integer.toHexString(bgr).toUpperCase() + "-r" + Integer.toHexString(r).toUpperCase() + "-g" + Integer.toHexString(g).toUpperCase() + "-b" + Integer.toHexString(b).toUpperCase();
        }
    }

    /*static TestColor[] examples = new TestColor[] {
        //            0xRRGGBB, 0xBBGGRR, 0xRR, 0xGG, 0xBB
        new TestColor(0xFFFFFF, 0xFFFFFF, 0xFF, 0xFF, 0xFF),
        new TestColor(0xFFFFAA, 0xAAFFFF, 0xFF, 0xFF, 0xAA),
        new TestColor(0xFF00FF, 0xFF00FF, 0xFF, 0x00, 0xFF),
        new TestColor(0x67FF22, 0x22FF67, 0x67, 0xFF, 0x22),
        new TestColor(0x000000, 0x000000, 0x00, 0x00, 0x00)
    };

    @Test
    public void testSerialization() throws Throwable {
        for (TestColor testColor : examples) {
            Color base = Color.fromRGB(testColor.rgb);

            YamlConfiguration toSerialize = new YamlConfiguration();
            toSerialize.set("color", base);
            String serialized = toSerialize.saveToString();

            YamlConfiguration deserialized = new YamlConfiguration();
            deserialized.loadFromString(serialized);

            assertThat(testColor.name + " on " + serialized, base, is(deserialized.getColor("color")));
        }
    }

    // Equality tests
    @Test
    public void testEqualities() {
        for (TestColor testColor : examples) {
            Color fromRGB = Color.fromRGB(testColor.rgb);
            Color fromBGR = Color.fromBGR(testColor.bgr);
            Color fromRGBs = Color.fromRGB(testColor.r, testColor.g, testColor.b);
            Color fromBGRs = Color.fromBGR(testColor.b, testColor.g, testColor.r);

            assertThat(testColor.name, fromRGB, is(fromRGBs));
            assertThat(testColor.name, fromRGB, is(fromBGR));
            assertThat(testColor.name, fromRGB, is(fromBGRs));
            assertThat(testColor.name, fromRGBs, is(fromBGR));
            assertThat(testColor.name, fromRGBs, is(fromBGRs));
            assertThat(testColor.name, fromBGR, is(fromBGRs));
        }
    }

    @Test
    public void testInequalities() {
        for (int i = 1; i < examples.length; i++) {
            TestColor testFrom = examples[i];
            Color from = Color.fromRGB(testFrom.rgb);
            for (int j = i - 1; j >= 0; j--) {
                TestColor testTo = examples[j];
                Color to = Color.fromRGB(testTo.rgb);
                String name = testFrom.name + " to " + testTo.name;
                assertThat(name, from, is(not(to)));

                Color transform = from.setRed(testTo.r).setBlue(testTo.b).setGreen(testTo.g);
                assertThat(name, transform, is(not(sameInstance(from))));
                assertThat(name, transform, is(to));
            }
        }
    }

    // RGB tests
    @Test
    public void testRGB() {
        for (TestColor testColor : examples) {
            assertThat(testColor.name, Color.fromRGB(testColor.rgb).asRGB(), is(testColor.rgb));
            assertThat(testColor.name, Color.fromBGR(testColor.bgr).asRGB(), is(testColor.rgb));
            assertThat(testColor.name, Color.fromRGB(testColor.r, testColor.g, testColor.b).asRGB(), is(testColor.rgb));
            assertThat(testColor.name, Color.fromBGR(testColor.b, testColor.g, testColor.r).asRGB(), is(testColor.rgb));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidRGB1() {
        Color.fromRGB(0x01000000);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidRGB2() {
        Color.fromRGB(Integer.MIN_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidRGB3() {
        Color.fromRGB(Integer.MAX_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidRGB4() {
        Color.fromRGB(-1);
    }

    // BGR tests
    @Test
    public void testBGR() {
        for (TestColor testColor : examples) {
            assertThat(testColor.name, Color.fromRGB(testColor.rgb).asBGR(), is(testColor.bgr));
            assertThat(testColor.name, Color.fromBGR(testColor.bgr).asBGR(), is(testColor.bgr));
            assertThat(testColor.name, Color.fromRGB(testColor.r, testColor.g, testColor.b).asBGR(), is(testColor.bgr));
            assertThat(testColor.name, Color.fromBGR(testColor.b, testColor.g, testColor.r).asBGR(), is(testColor.bgr));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidBGR1() {
        Color.fromBGR(0x01000000);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidBGR2() {
        Color.fromBGR(Integer.MIN_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidBGR3() {
        Color.fromBGR(Integer.MAX_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidBGR4() {
        Color.fromBGR(-1);
    }

    // Red tests
    @Test
    public void testRed() {
        for (TestColor testColor : examples) {
            assertThat(testColor.name, Color.fromRGB(testColor.rgb).getRed(), is(testColor.r));
            assertThat(testColor.name, Color.fromBGR(testColor.bgr).getRed(), is(testColor.r));
            assertThat(testColor.name, Color.fromRGB(testColor.r, testColor.g, testColor.b).getRed(), is(testColor.r));
            assertThat(testColor.name, Color.fromBGR(testColor.b, testColor.g, testColor.r).getRed(), is(testColor.r));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidR01() {
        Color.fromRGB(-1, 0x00, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidR02() {
        Color.fromRGB(Integer.MAX_VALUE, 0x00, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidR03() {
        Color.fromRGB(Integer.MIN_VALUE, 0x00, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidR04() {
        Color.fromRGB(0x100, 0x00, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidR05() {
        Color.fromBGR(0x00, 0x00, -1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidR06() {
        Color.fromBGR(0x00, 0x00, Integer.MAX_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidR07() {
        Color.fromBGR(0x00, 0x00, Integer.MIN_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidR08() {
        Color.fromBGR(0x00, 0x00, 0x100);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidR09() {
        Color.WHITE.setRed(-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidR10() {
        Color.WHITE.setRed(Integer.MAX_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidR11() {
        Color.WHITE.setRed(Integer.MIN_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidR12() {
        Color.WHITE.setRed(0x100);
    }

    // Blue tests
    @Test
    public void testBlue() {
        for (TestColor testColor : examples) {
            assertThat(testColor.name, Color.fromRGB(testColor.rgb).getBlue(), is(testColor.b));
            assertThat(testColor.name, Color.fromBGR(testColor.bgr).getBlue(), is(testColor.b));
            assertThat(testColor.name, Color.fromRGB(testColor.r, testColor.g, testColor.b).getBlue(), is(testColor.b));
            assertThat(testColor.name, Color.fromBGR(testColor.b, testColor.g, testColor.r).getBlue(), is(testColor.b));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidB01() {
        Color.fromRGB(0x00, 0x00, -1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidB02() {
        Color.fromRGB(0x00, 0x00, Integer.MAX_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidB03() {
        Color.fromRGB(0x00, 0x00, Integer.MIN_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidB04() {
        Color.fromRGB(0x00, 0x00, 0x100);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidB05() {
        Color.fromBGR(-1, 0x00, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidB06() {
        Color.fromBGR(Integer.MAX_VALUE, 0x00, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidB07() {
        Color.fromBGR(Integer.MIN_VALUE, 0x00, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidB08() {
        Color.fromBGR(0x100, 0x00, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidB09() {
        Color.WHITE.setBlue(-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidB10() {
        Color.WHITE.setBlue(Integer.MAX_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidB11() {
        Color.WHITE.setBlue(Integer.MIN_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidB12() {
        Color.WHITE.setBlue(0x100);
    }

    // Green tests
    @Test
    public void testGreen() {
        for (TestColor testColor : examples) {
            assertThat(testColor.name, Color.fromRGB(testColor.rgb).getGreen(), is(testColor.g));
            assertThat(testColor.name, Color.fromBGR(testColor.bgr).getGreen(), is(testColor.g));
            assertThat(testColor.name, Color.fromRGB(testColor.r, testColor.g, testColor.b).getGreen(), is(testColor.g));
            assertThat(testColor.name, Color.fromBGR(testColor.b, testColor.g, testColor.r).getGreen(), is(testColor.g));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidG01() {
        Color.fromRGB(0x00, -1, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidG02() {
        Color.fromRGB(0x00, Integer.MAX_VALUE, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidG03() {
        Color.fromRGB(0x00, Integer.MIN_VALUE, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidG04() {
        Color.fromRGB(0x00, 0x100, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidG05() {
        Color.fromBGR(0x00, -1, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidG06() {
        Color.fromBGR(0x00, Integer.MAX_VALUE, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidG07() {
        Color.fromBGR(0x00, Integer.MIN_VALUE, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidG08() {
        Color.fromBGR(0x00, 0x100, 0x00);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidG09() {
        Color.WHITE.setGreen(-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidG10() {
        Color.WHITE.setGreen(Integer.MAX_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidG11() {
        Color.WHITE.setGreen(Integer.MIN_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidG12() {
        Color.WHITE.setGreen(0x100);
    }*/


    // TODO: REWRITE - ask teacher if its ok to go the other way around instead of doing max(255, 255, 255) = 255 tests - just test the invalid values?
    /*@Test public void WE1_Valid() {
        assertDoesNotThrow(() -> Color.fromRGB(128, 128, 128));
    }

    @Test public void WE2_InvalidRed_Low() {
        assertThrows(IllegalArgumentException.class, () -> Color.fromRGB(-1, 128, 128));
    }

    @Test public void WE3_InvalidRed_High() {
        assertThrows(IllegalArgumentException.class, () -> Color.fromRGB(256, 128, 128));
    }

    @Test public void WE4_InvalidGreen_Low() {
        assertThrows(IllegalArgumentException.class, () -> Color.fromRGB(128, -1, 128));
    }

    @Test public void WE5_InvalidGreen_High() {
        assertThrows(IllegalArgumentException.class, () -> Color.fromRGB(128, 256, 128));
    }

    @Test public void WE6_InvalidBlue_Low() {
        assertThrows(IllegalArgumentException.class, () -> Color.fromRGB(128, 128, -1));
    }


    @Test public void WE7_InvalidBlue_High() {
        assertThrows(IllegalArgumentException.class, () -> Color.fromRGB(128, 128, 256));
    }*/

    @Test
    public void WE1_invalidLowerClass_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> Color.fromRGB(-1, -1, -1));
    }

    @Test
    public void WE2_validClass_createsColor() {
        Color color = Color.fromRGB(128, 128, 128);

        assertAll("Color components",
            () -> assertNotNull(color, "Color object should not be null"),
            () -> assertEquals(128, color.getRed(),   "Red component mismatch"),
            () -> assertEquals(128, color.getGreen(), "Green component mismatch"),
            () -> assertEquals(128, color.getBlue(),  "Blue component mismatch")
        );
    }

    @Test
    public void WE3_invalidUpperClass_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> Color.fromRGB(256, 256, 256));
    }

    @ParameterizedTest(name = "{0}: fromRGB({1}, {2}, {3}) should throw")
    @CsvSource({
            "SE1,  -1,  -1,  -1",
            "SE2,  -1,  -1, 128",
            "SE3,  -1,  -1, 256",
            "SE4,  -1, 128,  -1",
            "SE5,  -1, 128, 128",
            "SE6,  -1, 128, 256",
            "SE7,  -1, 256,  -1",
            "SE8,  -1, 256, 128",
            "SE9,  -1, 256, 256",
            "SE10, 128,  -1,  -1",
            "SE11, 128,  -1, 128",
            "SE12, 128,  -1, 256",
            "SE13, 128, 128,  -1",
            "SE15, 128, 128, 256",
            "SE16, 128, 256,  -1",
            "SE17, 128, 256, 128",
            "SE18, 128, 256, 256",
            "SE19, 256,  -1,  -1",
            "SE20, 256,  -1, 128",
            "SE21, 256,  -1, 256",
            "SE22, 256, 128,  -1",
            "SE23, 256, 128, 128",
            "SE24, 256, 128, 256",
            "SE25, 256, 256,  -1",
            "SE26, 256, 256, 128",
            "SE27, 256, 256, 256"
    })
    public void invalidCombinations_throwException(String id, int red, int green, int blue) {
        assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(red, green, blue));
    }

    @ParameterizedTest(name = "{0}: fromRGB({1}, {2}, {3}) should succeed")
    @CsvSource({
            "SE14, 128, 128, 128"
    })
    public void validCombination_createsColor(String id, int red, int green, int blue) {
        Color color = Color.fromRGB(red, green, blue);
        assertNotNull(color);
    }

    //TODO: BVA testing (im not sure about this, i'll ask teacher thursday)
    @ParameterizedTest(name = "{0}: fromRGB({1}, {2}, {3}) should succeed")
    @CsvSource({
            "SBVA1,  0,   128, 128",
            "SBVA2,  1,   128, 128",
            "SBVA3,  254, 128, 128",
            "SBVA4,  255, 128, 128",
            "SBVA5,  128, 0,   128",
            "SBVA6,  128, 1,   128",
            "SBVA7,  128, 254, 128",
            "SBVA8,  128, 255, 128",
            "SBVA9,  128, 128, 0",
            "SBVA10, 128, 128, 1",
            "SBVA11, 128, 128, 254",
            "SBVA12, 128, 128, 255",
            "SBVA13, 128, 128, 128"
    })
    public void simpleBVA_validBoundaries_createColor(String id, int red, int green, int blue) {
        Color color = Color.fromRGB(red, green, blue);
        assertNotNull(color);
    }

    @ParameterizedTest(name = "{0}: fromRGB({1}, {2}, {3}) should succeed")
    @CsvSource({
            "RBVA2,  0,   128, 128",
            "RBVA3,  1,   128, 128",
            "RBVA4,  254, 128, 128",
            "RBVA5,  255, 128, 128",
            "RBVA8,  128, 0,   128",
            "RBVA9,  128, 1,   128",
            "RBVA10, 128, 254, 128",
            "RBVA11, 128, 255, 128",
            "RBVA14, 128, 128, 0",
            "RBVA15, 128, 128, 1",
            "RBVA16, 128, 128, 254",
            "RBVA17, 128, 128, 255",
            "RBVA19, 128, 128, 128"
    })
    public void robustBVA_validBoundaries_createColor(String id, int red, int green, int blue) {
        Color color = Color.fromRGB(red, green, blue);
        assertNotNull(color);
    }

    @ParameterizedTest(name = "{0}: fromRGB({1}, {2}, {3}) should throw")
    @CsvSource({
            "RBVA1,  -1,  128, 128",
            "RBVA6,  256, 128, 128",
            "RBVA7,  128, -1,  128",
            "RBVA12, 128, 256, 128",
            "RBVA13, 128, 128, -1",
            "RBVA18, 128, 128, 256"
    })
    public void robustBVA_invalidBoundaries_throwException(String id, int red, int green, int blue) {
        assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(red, green, blue));
    }

    // ==================== Worst Case BVA (125 tests) ====================
    private static Stream<Arguments> worstCaseBVAProvider() {
        int[] values = {0, 1, 128, 254, 255};
        List<Arguments> args = new ArrayList<>();
        int id = 1;
        for (int r : values) {
            for (int g : values) {
                for (int b : values) {
                    args.add(Arguments.of("WCBVA" + id++, r, g, b));
                }
            }
        }
        return args.stream();
    }

    @ParameterizedTest(name = "{0}: fromRGB({1}, {2}, {3}) should succeed")
    @MethodSource("worstCaseBVAProvider")
    public void worstCaseBVA_allCombinations_createColor(String id, int red, int green, int blue) {
        Color color = Color.fromRGB(red, green, blue);
        assertNotNull(color);
    }

    // ==================== Robust Worst Case BVA (343 tests) ====================
    private static Stream<Arguments> robustWorstCaseBVAProvider() {
        int[] values = {-1, 0, 1, 128, 254, 255, 256};
        List<Arguments> args = new ArrayList<>();
        int id = 1;
        for (int r : values) {
            for (int g : values) {
                for (int b : values) {
                    args.add(Arguments.of("RWCBVA" + id++, r, g, b));
                }
            }
        }
        return args.stream();
    }

    @ParameterizedTest(name = "{0}: fromRGB({1}, {2}, {3})")
    @MethodSource("robustWorstCaseBVAProvider")
    public void robustWorstCaseBVA_allCombinations(String id, int red, int green, int blue) {
        boolean allValid = (red >= 0 && red <= 255)
                        && (green >= 0 && green <= 255)
                        && (blue >= 0 && blue <= 255);

        if (allValid) {
            Color color = Color.fromRGB(red, green, blue);
            assertNotNull(color);
        } else {
            assertThrows(IllegalArgumentException.class,
                    () -> Color.fromRGB(red, green, blue));
        }
    }

    // CONTROL FLOW TESTS
    // statement coverage
    @Test
    public void SN1_fromRGB_ValidInput_ReturnsColor() {
        Color c = Color.fromRGB(100, 150, 200);
        assertNotNull(c, "Valid RGB values should create a Color");
    }

    @Test
    public void SN2_fromRGB_AllZero_ReturnsColor() {
        Color c = Color.fromRGB(0, 0, 0);
        assertNotNull(c, "All zero values should create a Color");
    }

    @Test
    public void SN3_fromRGB_AllMax_ReturnsColor() {
        Color c = Color.fromRGB(255, 255, 255);
        assertNotNull(c, "All max values (255) should create a Color");
    }

    @Test
    public void SN4_fromRGB_RedNegative_ThrowsWithRedMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(-1, 150, 200));
        assertTrue(ex.getMessage().contains("Red"),
                "D1=false (red validation), D2=true, D3=true: Red should be the failing decision");
    }

    @Test
    public void SN5_fromRGB_RedAbove255_ThrowsWithRedMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(256, 150, 200));
        assertTrue(ex.getMessage().contains("Red"),
                "D1=false (red validation), D2=true, D3=true: Red should be the failing decision");
    }

    @Test
    public void SN6_fromRGB_GreenNegative_ThrowsWithGreenMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(100, -1, 200));
        assertTrue(ex.getMessage().contains("Green"),
                "D1=true, D2=false (green validation), D3=true: Green should be the failing decision");
    }

    @Test
    public void SN7_fromRGB_GreenAbove255_ThrowsWithGreenMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(100, 256, 200));
        assertTrue(ex.getMessage().contains("Green"),
                "D1=true, D2=false (green validation), D3=true: Green should be the failing decision");
    }

    @Test
    public void SN8_fromRGB_BlueNegative_ThrowsWithBlueMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(100, 150, -1));
        assertTrue(ex.getMessage().contains("Blue"),
                "D1=true, D2=true, D3=false (blue validation): Blue should be the failing decision");
    }

    @Test
    public void SN9_fromRGB_BlueAbove255_ThrowsWithBlueMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(100, 150, 300));
        assertTrue(ex.getMessage().contains("Blue"),
                "D1=true, D2=true, D3=false (blue validation): Blue should be the failing decision");
    }

    // Atomic conditions to cover:
    // red >= 0, red <= BIT_MASK
    // green >= 0, green <= BIT_MASK
    // blue >= 0, blue <= BIT_MASK
    // Each must be evaluated to TRUE and FALSE at least once.

    // --- Baseline: all conditions TRUE ---
    @Test
    public void CN1_fromRGB_AllValid_ReturnsColor() {
        Color c = Color.fromRGB(100, 150, 200);
        assertNotNull(c, "All conditions TRUE: valid RGB creates Color");
    }

    // --- Red conditions ---
    @Test
    public void CN2_fromRGB_RedNegative_RedGe0False() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(-1, 150, 200));
        assertTrue(ex.getMessage().contains("Red"),
                "Condition red >= 0 evaluated to FALSE");
    }

    @Test
    public void CN3_fromRGB_Red256_RedLe255False() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(256, 150, 200));
        assertTrue(ex.getMessage().contains("Red"),
                "Condition red <= BIT_MASK evaluated to FALSE");
    }

    // --- Green conditions ---
    @Test
    public void CN4_fromRGB_GreenNegative_GreenGe0False() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(100, -1, 200));
        assertTrue(ex.getMessage().contains("Green"),
                "Condition green >= 0 evaluated to FALSE");
    }

    @Test
    public void CN5_fromRGB_Green256_GreenLe255False() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(100, 256, 200));
        assertTrue(ex.getMessage().contains("Green"),
                "Condition green <= BIT_MASK evaluated to FALSE");
    }

    // --- Blue conditions ---
    @Test
    public void CN6_fromRGB_BlueNegative_BlueGe0False() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(100, 150, -1));
        assertTrue(ex.getMessage().contains("Blue"),
                "Condition blue >= 0 evaluated to FALSE");
    }

    @Test
    public void CN7_fromRGB_Blue300_BlueLe255False() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Color.fromRGB(100, 150, 300));
        assertTrue(ex.getMessage().contains("Blue"),
                "Condition blue <= BIT_MASK evaluated to FALSE");
    }
}
