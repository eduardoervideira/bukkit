package org.bukkit;

import org.bukkit.util.ChatPaginator;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Pairwise tests for {@link ChatPaginator#wordWrap(String, int)}.
 *
 * The test cases follow the pairwise table:
 *  - rawString  in { null, "", "bukkit", "bukkit bukkit bukkit",
 *                    "bukkit\nbukkit", "§abukkit",
 *                    "bukkit   bukkit", "   " }
 *  - lineLength in { 0, 1, 10 }
 *
 * The Bukkit chat color escape char (§, the section sign "§") is written
 * as the constant COLOR for readability. ChatColor.WHITE.toString() = "§f"
 * (the default color prepended to lines that don't already start with a color).
 *
 * Each test documents the expected outcome in a comment, derived by tracing
 * the implementation in src/main/java/org/bukkit/util/ChatPaginator.java.
 * Several lineLength=0 / lineLength=1 cases exercise degenerate paths and
 * reveal latent defects (StringIndexOutOfBoundsException), so those cases
 * assert that the expected exception is thrown.
 */
public class ChatPaginatorWordWrapPairwiseTest {

    private static final String WHITE = ChatColor.WHITE.toString(); // "§f"
    private static final char COLOR = ChatColor.COLOR_CHAR;          // '§'

    // -------------------------------------------------------------------------
    // Case 1: rawString = null, lineLength = 0
    // Expected: {""} — null short-circuits to a single empty line, no color.
    // -------------------------------------------------------------------------
    @Test
    public void test01_NullZero() {
        String[] lines = ChatPaginator.wordWrap(null, 0);
        assertArrayEquals(new String[] {""}, lines);
    }

    // -------------------------------------------------------------------------
    // Case 2: rawString = null, lineLength = 1
    // Expected: {""} — null is always handled by the first guard clause.
    // -------------------------------------------------------------------------
    @Test
    public void test02_NullOne() {
        String[] lines = ChatPaginator.wordWrap(null, 1);
        assertArrayEquals(new String[] {""}, lines);
    }

    // -------------------------------------------------------------------------
    // Case 3: rawString = null, lineLength = 10
    // Expected: {""} — null is always handled by the first guard clause.
    // -------------------------------------------------------------------------
    @Test
    public void test03_NullTen() {
        String[] lines = ChatPaginator.wordWrap(null, 10);
        assertArrayEquals(new String[] {""}, lines);
    }

    // -------------------------------------------------------------------------
    // Case 4: rawString = "", lineLength = 0
    // Expected: {""} — length 0 <= 0, no '\n', returns the raw string as-is.
    // -------------------------------------------------------------------------
    @Test
    public void test04_EmptyZero() {
        String[] lines = ChatPaginator.wordWrap("", 0);
        assertArrayEquals(new String[] {""}, lines);
    }

    // -------------------------------------------------------------------------
    // Case 5: rawString = "", lineLength = 1
    // Expected: {""} — early return on length <= lineLength.
    // -------------------------------------------------------------------------
    @Test
    public void test05_EmptyOne() {
        String[] lines = ChatPaginator.wordWrap("", 1);
        assertArrayEquals(new String[] {""}, lines);
    }

    // -------------------------------------------------------------------------
    // Case 6: rawString = "", lineLength = 10
    // Expected: {""} — early return on length <= lineLength.
    // -------------------------------------------------------------------------
    @Test
    public void test06_EmptyTen() {
        String[] lines = ChatPaginator.wordWrap("", 10);
        assertArrayEquals(new String[] {""}, lines);
    }

    // -------------------------------------------------------------------------
    // Case 7: rawString = "bukkit", lineLength = 0
    // Expected: {"§fbukkit"} — the long-word branch splits "bukkit" by the
    // regex "(?<=\\G.{0})", which in Java is a zero-width match that yields
    // the original string unchanged. So "bukkit" stays on one line and
    // gets the default WHITE prefix.
    // -------------------------------------------------------------------------
    @Test
    public void test07_BukkitZero() {
        String[] lines = ChatPaginator.wordWrap("bukkit", 0);
        assertArrayEquals(new String[] { WHITE + "bukkit" }, lines);
    }

    // -------------------------------------------------------------------------
    // Case 8: rawString = "bukkit", lineLength = 1
    // Expected: same 6 single-char lines — splitting by .{1} also yields chars.
    //   {"§fb", "§fu", "§fk", "§fk", "§fi", "§ft"}
    // -------------------------------------------------------------------------
    @Test
    public void test08_BukkitOne() {
        String[] lines = ChatPaginator.wordWrap("bukkit", 1);
        assertArrayEquals(new String[] {
                WHITE + "b", WHITE + "u", WHITE + "k",
                WHITE + "k", WHITE + "i", WHITE + "t"
        }, lines);
    }

    // -------------------------------------------------------------------------
    // Case 9: rawString = "bukkit", lineLength = 10
    // Expected: {"bukkit"} — the early-return path returns the raw string
    // WITHOUT prepending a color (a subtle inconsistency with the main path).
    // -------------------------------------------------------------------------
    @Test
    public void test09_BukkitTen() {
        String[] lines = ChatPaginator.wordWrap("bukkit", 10);
        assertArrayEquals(new String[] {"bukkit"}, lines);
    }

    // -------------------------------------------------------------------------
    // Case 10: rawString = "bukkit bukkit bukkit", lineLength = 0
    // Expected: {"§fbukkit","§fbukkit","§fbukkit"} — at each space the
    // long-word branch is entered, but the .{0} split is a no-op so each
    // word is emitted whole. Each gets WHITE prepended.
    // -------------------------------------------------------------------------
    @Test
    public void test10_ThreeBukkitsZero() {
        String[] lines = ChatPaginator.wordWrap("bukkit bukkit bukkit", 0);
        assertArrayEquals(new String[] {
                WHITE + "bukkit", WHITE + "bukkit", WHITE + "bukkit"
        }, lines);
    }

    // -------------------------------------------------------------------------
    // Case 11: rawString = "bukkit bukkit bukkit", lineLength = 1
    // Expected: same 18 single-char lines — .{1} splits each long word per char.
    // -------------------------------------------------------------------------
    @Test
    public void test11_ThreeBukkitsOne() {
        String[] lines = ChatPaginator.wordWrap("bukkit bukkit bukkit", 1);
        String[] expected = new String[18];
        String src = "bukkitbukkitbukkit";
        for (int i = 0; i < 18; i++) {
            expected[i] = WHITE + src.charAt(i);
        }
        assertArrayEquals(expected, lines);
    }

    // -------------------------------------------------------------------------
    // Case 12: rawString = "bukkit bukkit bukkit", lineLength = 10
    // Expected: {"§fbukkit", "§fbukkit", "§fbukkit"} — each "bukkit" + space
    // would exceed 10, so lines are broken on spaces.
    // -------------------------------------------------------------------------
    @Test
    public void test12_ThreeBukkitsTen() {
        String[] lines = ChatPaginator.wordWrap("bukkit bukkit bukkit", 10);
        assertArrayEquals(new String[] {
                WHITE + "bukkit", WHITE + "bukkit", WHITE + "bukkit"
        }, lines);
    }

    // -------------------------------------------------------------------------
    // Case 13: rawString = "bukkit\nbukkit", lineLength = 0
    // Expected: {"§fbukkit","§f","§fbukkit"} — first word emitted whole
    // (long-word branch with no-op .{0} split), then '\n' pushes an empty
    // line, then the second word emitted whole. Color phase prepends WHITE.
    // -------------------------------------------------------------------------
    @Test
    public void test13_BukkitNewlineBukkitZero() {
        String[] lines = ChatPaginator.wordWrap("bukkit\nbukkit", 0);
        assertArrayEquals(new String[] {
                WHITE + "bukkit", WHITE, WHITE + "bukkit"
        }, lines);
    }

    // -------------------------------------------------------------------------
    // Case 14: rawString = "bukkit\nbukkit", lineLength = 1
    // Expected: same 13 entries as case 13 (split by .{1} also yields chars).
    // -------------------------------------------------------------------------
    @Test
    public void test14_BukkitNewlineBukkitOne() {
        String[] lines = ChatPaginator.wordWrap("bukkit\nbukkit", 1);
        assertArrayEquals(new String[] {
                WHITE + "b", WHITE + "u", WHITE + "k",
                WHITE + "k", WHITE + "i", WHITE + "t",
                WHITE,
                WHITE + "b", WHITE + "u", WHITE + "k",
                WHITE + "k", WHITE + "i", WHITE + "t"
        }, lines);
    }

    // -------------------------------------------------------------------------
    // Case 15: rawString = "bukkit\nbukkit", lineLength = 10
    // Expected: {"§fbukkit", "§fbukkit"} — '\n' just flushes the line.
    // -------------------------------------------------------------------------
    @Test
    public void test15_BukkitNewlineBukkitTen() {
        String[] lines = ChatPaginator.wordWrap("bukkit\nbukkit", 10);
        assertArrayEquals(new String[] {
                WHITE + "bukkit", WHITE + "bukkit"
        }, lines);
    }

    // -------------------------------------------------------------------------
    // Case 16: rawString = "§abukkit", lineLength = 0
    // Expected: {"§abukkit"} — the long-word branch's .{0} split is a no-op,
    // so the word with its embedded color is emitted as a single line. Since
    // it already begins with COLOR_CHAR, the WHITE default is NOT prepended.
    // -------------------------------------------------------------------------
    @Test
    public void test16_ColorBukkitZero() {
        String[] lines = ChatPaginator.wordWrap(COLOR + "abukkit", 0);
        assertArrayEquals(new String[] { COLOR + "abukkit" }, lines);
    }

    // -------------------------------------------------------------------------
    // Case 17: rawString = "§abukkit", lineLength = 1
    // Expected: StringIndexOutOfBoundsException — same defect as case 16.
    // -------------------------------------------------------------------------
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void test17_ColorBukkitOne() {
        ChatPaginator.wordWrap(COLOR + "abukkit", 1);
    }

    // -------------------------------------------------------------------------
    // Case 18: rawString = "§abukkit", lineLength = 10
    // Expected: {"§abukkit"} — length 8 <= 10, no '\n', early return as-is.
    // -------------------------------------------------------------------------
    @Test
    public void test18_ColorBukkitTen() {
        String[] lines = ChatPaginator.wordWrap(COLOR + "abukkit", 10);
        assertArrayEquals(new String[] {COLOR + "abukkit"}, lines);
    }

    // -------------------------------------------------------------------------
    // Case 19: rawString = "bukkit   bukkit" (3 spaces), lineLength = 0
    // Expected: {"§fbukkit","§f","§f","§fbukkit"} — first word emitted whole,
    // then the 2nd and 3rd spaces hit the "line == lineLength" branch with
    // both line and word empty (so an empty line is added each time), then
    // the second word emitted whole. Color phase prepends WHITE.
    // -------------------------------------------------------------------------
    @Test
    public void test19_BukkitSpacesBukkitZero() {
        String[] lines = ChatPaginator.wordWrap("bukkit   bukkit", 0);
        assertArrayEquals(new String[] {
                WHITE + "bukkit", WHITE, WHITE, WHITE + "bukkit"
        }, lines);
    }

    // -------------------------------------------------------------------------
    // Case 20: rawString = "bukkit   bukkit", lineLength = 1
    // Expected: 12 single chars (no empty entries — with lineLength=1 the
    // extra spaces fall to the else-branch which is a no-op on empty word).
    // -------------------------------------------------------------------------
    @Test
    public void test20_BukkitSpacesBukkitOne() {
        String[] lines = ChatPaginator.wordWrap("bukkit   bukkit", 1);
        String[] expected = new String[12];
        String src = "bukkitbukkit";
        for (int i = 0; i < 12; i++) {
            expected[i] = WHITE + src.charAt(i);
        }
        assertArrayEquals(expected, lines);
    }

    // -------------------------------------------------------------------------
    // Case 21: rawString = "bukkit   bukkit", lineLength = 10
    // Expected: {"§fbukkit  ", "§fbukkit"} — after the first "bukkit" is
    // appended, the next two spaces append space chars to the line; then the
    // second "bukkit" would push past 10, so the line is flushed (with its
    // trailing spaces preserved) and the second word starts a new line.
    // -------------------------------------------------------------------------
    @Test
    public void test21_BukkitSpacesBukkitTen() {
        String[] lines = ChatPaginator.wordWrap("bukkit   bukkit", 10);
        assertArrayEquals(new String[] {
                WHITE + "bukkit  ", WHITE + "bukkit"
        }, lines);
    }

    // -------------------------------------------------------------------------
    // Case 22: rawString = "   " (3 spaces), lineLength = 0
    // Expected: 4 empty lines (3 input spaces + the trailing space the method
    // appends), each hitting the "line == lineLength" branch with empty
    // line/word. Each becomes WHITE-only after color processing.
    //   {"§f", "§f", "§f", "§f"}
    // -------------------------------------------------------------------------
    @Test
    public void test22_SpacesZero() {
        String[] lines = ChatPaginator.wordWrap("   ", 0);
        assertArrayEquals(new String[] {WHITE, WHITE, WHITE, WHITE}, lines);
    }

    // -------------------------------------------------------------------------
    // Case 23: rawString = "   ", lineLength = 1
    // Expected: IndexOutOfBoundsException — every iteration falls into the
    // else branch with line and word both empty (no-op). Nothing is ever
    // added to `lines`, so `lines.get(0)` at the start of color propagation
    // throws IndexOutOfBoundsException. Defect revealed by pairwise testing.
    // -------------------------------------------------------------------------
    @Test(expected = IndexOutOfBoundsException.class)
    public void test23_SpacesOne() {
        ChatPaginator.wordWrap("   ", 1);
    }

    // -------------------------------------------------------------------------
    // Case 24: rawString = "   ", lineLength = 10
    // Expected: {"   "} — length 3 <= 10, no '\n', early return as-is.
    // -------------------------------------------------------------------------
    @Test
    public void test24_SpacesTen() {
        String[] lines = ChatPaginator.wordWrap("   ", 10);
        assertArrayEquals(new String[] {"   "}, lines);
    }
}
