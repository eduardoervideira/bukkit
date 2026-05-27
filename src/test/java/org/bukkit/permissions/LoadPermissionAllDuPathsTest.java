package org.bukkit.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.TestServer;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Data-flow tests for {@link Permission#loadPermission(String, Map, PermissionDefault, java.util.List)}.
 *
 * Criterion: all-def-use-paths. For every (definition d, use u) DU pair,
 * exercise EVERY simple def-clear path from d to u (loop bounded to 0 or 1
 * iteration, since multiple iterations revisit the loop header and aren't
 * simple paths).
 *
 * Path-encoding alphabet (only the branches between d and u that admit a
 * def-clear path appear in any given pair's enumeration):
 *   D0  = no "default" key
 *   DV  = "default" = "OP" (valid PermissionDefault name; d2 of `def` fires
 *         and value != null)
 *   C0  = no "children" key
 *   CIe = "children" = empty Iterable
 *   CIn = "children" = Iterable with one null element
 *   CIv = "children" = Iterable with one non-null String element
 *   CM  = "children" = Map with one entry
 *   E0  = no "description" key
 *   EV  = "description" present
 *
 * One @Test method per DU pair; each method iterates all simple def-clear
 * paths for that pair and runs the call along each.
 *
 * Excluded pairs (no def-clear path, documented in [[LoadPermissionAllUsesTest]]):
 *   - children d1 -> 302 (killed by d2 inside the Iterable branch)
 *   - children d3 -> 302 (mutually exclusive branches)
 */
public class LoadPermissionAllDuPathsTest {

    @BeforeClass
    public static void setUp() {
        TestServer.getInstance();
    }

    // ===== input builders =====

    private static Map<String, Object> data(String def, String ch, String desc) {
        Map<String, Object> m = new HashMap<String, Object>();
        if ("DV".equals(def)) m.put("default", "OP");
        if ("CIe".equals(ch)) m.put("children", Collections.emptyList());
        else if ("CIn".equals(ch)) m.put("children", Collections.singletonList(null));
        else if ("CIv".equals(ch)) m.put("children", Arrays.asList("kid"));
        else if ("CM".equals(ch)) m.put("children", Collections.singletonMap("kid", Boolean.TRUE));
        if ("EV".equals(desc)) m.put("description", "d");
        return m;
    }

    /** Children as Map with a nested empty-data Map; forces extractChildren to
     *  invoke loadPermission(..., def, output) so the outer `def` and `output`
     *  values are externally observable on the nested permission. */
    private static Map<String, Object> dataWithNestedMapChildren(String def, String desc) {
        Map<String, Object> nested = new HashMap<String, Object>();
        Map<String, Object> children = new LinkedHashMap<String, Object>();
        children.put("kid", nested);
        Map<String, Object> m = new HashMap<String, Object>();
        if ("DV".equals(def)) m.put("default", "OP");
        m.put("children", children);
        if ("EV".equals(desc)) m.put("description", "d");
        return m;
    }

    private static final String[] D_ALL = {"D0", "DV"};
    private static final String[] C_ALL = {"C0", "CIe", "CIn", "CIv", "CM"};
    private static final String[] E_ALL = {"E0", "EV"};

    // ===================================================================
    // variable: name  (d1 = parameter)
    // ===================================================================

    @Test
    public void allDuPaths_name_d1_to_281() {
        // 1 path: linear, no branches before line 281.
        Permission r = Permission.loadPermission("p", data("D0","C0","E0"), PermissionDefault.OP, null);
        assertEquals("p", r.getName());
    }

    @Test
    public void allDuPaths_name_d1_to_306() {
        // 2 paths: {D0, DV} x CM
        for (String d : D_ALL) {
            String tag = d + "_CM";
            Permission r = Permission.loadPermission("p_"+tag, data(d, "CM", "E0"), PermissionDefault.OP, null);
            assertEquals(tag, "p_"+tag, r.getName());
        }
    }

    @Test
    public void allDuPaths_name_d1_to_316() {
        // 20 paths: D_ALL (2) x C_ALL (5) x E_ALL (2)
        for (String d : D_ALL)
            for (String c : C_ALL)
                for (String e : E_ALL) {
                    String tag = d + "_" + c + "_" + e;
                    Permission r = Permission.loadPermission("p_"+tag, data(d,c,e), PermissionDefault.OP, null);
                    assertEquals(tag, "p_"+tag, r.getName());
                }
    }

    // ===================================================================
    // variable: data  (d1 = parameter)
    // ===================================================================

    @Test
    public void allDuPaths_data_d1_to_282() {
        // 1 path: linear (Validate.notNull at 282 has no preceding branch).
        Permission r = Permission.loadPermission("p", data("D0","C0","E0"), PermissionDefault.OP, null);
        assertNotNull(r);
    }

    @Test
    public void allDuPaths_data_d1_to_287() {
        // 1 path: linear up to the `if (data.get("default") != null)` check.
        Permission r = Permission.loadPermission("p", data("D0","C0","E0"), PermissionDefault.OP, null);
        assertNotNull(r);
    }

    @Test
    public void allDuPaths_data_d1_to_288() {
        // 1 path: enter the "default" branch (any present value reaches the
        // use; branching at line 289 happens after the use).
        Permission r = Permission.loadPermission("p", data("DV","C0","E0"), PermissionDefault.FALSE, null);
        assertEquals(PermissionDefault.OP, r.getDefault());
    }

    @Test
    public void allDuPaths_data_d1_to_296() {
        // 2 paths: {D0, DV} (DX throws before 296, excluded).
        for (String d : D_ALL) {
            Permission r = Permission.loadPermission("p", data(d,"C0","E0"), PermissionDefault.OP, null);
            assertNotNull(d, r);
        }
    }

    @Test
    public void allDuPaths_data_d1_to_297() {
        // 2 paths: {D0, DV} x (children present, any kind reaches 297 first).
        for (String d : D_ALL) {
            Permission r = Permission.loadPermission("p", data(d,"CIe","E0"), PermissionDefault.OP, null);
            assertNotNull(d, r);
        }
    }

    @Test
    public void allDuPaths_data_d1_to_312() {
        // 10 paths: D_ALL x C_ALL.
        for (String d : D_ALL)
            for (String c : C_ALL) {
                String tag = d + "_" + c;
                Permission r = Permission.loadPermission("p", data(d,c,"E0"), PermissionDefault.OP, null);
                assertNotNull(tag, r);
            }
    }

    @Test
    public void allDuPaths_data_d1_to_313() {
        // 10 paths: D_ALL x C_ALL x EV.
        for (String d : D_ALL)
            for (String c : C_ALL) {
                String tag = d + "_" + c + "_EV";
                Permission r = Permission.loadPermission("p", data(d,c,"EV"), PermissionDefault.OP, null);
                assertEquals(tag, "d", r.getDescription());
            }
    }

    // ===================================================================
    // variable: def  (d1 = parameter, d2 = "def = value;" at 290)
    // ===================================================================

    @Test
    public void allDuPaths_def_d1_to_306() {
        // 1 path: D0 (d1 not killed) + CM (Map -> reaches 306).
        // Nested empty-data Map makes extractChildren forward `def` to a
        // recursive loadPermission, so the nested Permission's default proves
        // d1 reached the use.
        List<Permission> out = new ArrayList<Permission>();
        Permission.loadPermission("p", dataWithNestedMapChildren("D0","E0"),
                PermissionDefault.TRUE, out);
        assertEquals(PermissionDefault.TRUE, out.get(0).getDefault());
    }

    @Test
    public void allDuPaths_def_d1_to_316() {
        // 10 paths: D0 x C_ALL x E_ALL.
        for (String c : C_ALL)
            for (String e : E_ALL) {
                String tag = "D0_" + c + "_" + e;
                Permission r = Permission.loadPermission("p", data("D0", c, e), PermissionDefault.TRUE, null);
                assertEquals(tag, PermissionDefault.TRUE, r.getDefault());
            }
    }

    @Test
    public void allDuPaths_def_d2_to_306() {
        // 1 path: DV (d2 fires) + CM. Use nested-Map to observe d2's value
        // (OP) on the inner permission.
        List<Permission> out = new ArrayList<Permission>();
        Permission.loadPermission("p", dataWithNestedMapChildren("DV","E0"),
                PermissionDefault.FALSE, out);
        assertEquals(PermissionDefault.OP, out.get(0).getDefault());
    }

    @Test
    public void allDuPaths_def_d2_to_316() {
        // 10 paths: DV x C_ALL x E_ALL.
        for (String c : C_ALL)
            for (String e : E_ALL) {
                String tag = "DV_" + c + "_" + e;
                Permission r = Permission.loadPermission("p", data("DV", c, e), PermissionDefault.FALSE, null);
                assertEquals(tag, PermissionDefault.OP, r.getDefault());
            }
    }

    // ===================================================================
    // variable: output  (d1 = parameter)
    // ===================================================================

    @Test
    public void allDuPaths_output_d1_to_306() {
        // 2 paths: {D0, DV} + CM. Nested-Map child causes extractChildren to
        // append the nested permission to `output`, proving the parameter
        // reached the use.
        for (String d : D_ALL) {
            List<Permission> out = new ArrayList<Permission>();
            Permission.loadPermission("p", dataWithNestedMapChildren(d,"E0"),
                    PermissionDefault.FALSE, out);
            assertEquals(d, 1, out.size());
            assertEquals(d, "kid", out.get(0).getName());
        }
    }

    // ===================================================================
    // variable: desc  (d1 = null at 284, d2 = data.get("description") at 313)
    // ===================================================================

    @Test
    public void allDuPaths_desc_d1_to_316() {
        // 10 paths: D_ALL x C_ALL x E0 (skip 312 to keep d1 alive).
        for (String d : D_ALL)
            for (String c : C_ALL) {
                String tag = d + "_" + c + "_E0";
                Permission r = Permission.loadPermission("p", data(d,c,"E0"), PermissionDefault.OP, null);
                // ctor maps null description -> "".
                assertEquals(tag, "", r.getDescription());
            }
    }

    @Test
    public void allDuPaths_desc_d2_to_316() {
        // 1 path: from line 313 to 316 there are no branches.
        Permission r = Permission.loadPermission("p", data("D0","C0","EV"), PermissionDefault.OP, null);
        assertEquals("d", r.getDescription());
    }

    // ===================================================================
    // variable: children
    //   d1 = null at 285, d2 = new LinkedHashMap at 299, d3 = extractChildren at 306
    // ===================================================================

    @Test
    public void allDuPaths_children_d1_to_316() {
        // 4 paths: D_ALL x C0 x E_ALL (skip B296 to keep d1 alive).
        for (String d : D_ALL)
            for (String e : E_ALL) {
                String tag = d + "_C0_" + e;
                Permission r = Permission.loadPermission("p", data(d,"C0",e), PermissionDefault.OP, null);
                assertEquals(tag, 0, r.getChildren().size());
            }
    }

    @Test
    public void allDuPaths_children_d2_to_302() {
        // 1 path: Iterable with one non-null element (CIv) -- the only way the
        // for-body executes 302.
        Permission r = Permission.loadPermission("p", data("D0","CIv","E0"), PermissionDefault.OP, null);
        assertEquals(Boolean.TRUE, r.getChildren().get("kid"));
    }

    @Test
    public void allDuPaths_children_d2_to_316() {
        // 6 paths: {CIe, CIn, CIv} x E_ALL.
        for (String c : new String[]{"CIe","CIn","CIv"})
            for (String e : E_ALL) {
                String tag = c + "_" + e;
                Permission r = Permission.loadPermission("p", data("D0",c,e), PermissionDefault.OP, null);
                int expected = "CIv".equals(c) ? 1 : 0;
                assertEquals(tag, expected, r.getChildren().size());
            }
    }

    @Test
    public void allDuPaths_children_d3_to_316() {
        // 2 paths: CM x E_ALL.
        for (String e : E_ALL) {
            String tag = "CM_" + e;
            Permission r = Permission.loadPermission("p", data("D0","CM",e), PermissionDefault.OP, null);
            assertEquals(tag, Boolean.TRUE, r.getChildren().get("kid"));
        }
    }

    // ===================================================================
    // variable: value  (d1 = PermissionDefault.getByName(...) at 288)
    // ===================================================================

    @Test
    public void allDuPaths_value_d1_to_289() {
        // 1 path: 288 -> 289 (the null-check itself is the use). Use an
        // invalid name so the path exercises the false branch at 289, which
        // throws -- proving the null check on `value` was evaluated.
        Map<String, Object> dat = new HashMap<String, Object>();
        dat.put("default", "NOT_A_REAL_VALUE");
        try {
            Permission.loadPermission("p", dat, PermissionDefault.FALSE, null);
            fail("expected IllegalArgumentException from line 292");
        } catch (IllegalArgumentException expected) {
            // The throw at 292 is only reachable after the null-check at 289
            // evaluated `value`.
        }
    }

    @Test
    public void allDuPaths_value_d1_to_290() {
        // 1 path: 288 -> 289(true) -> 290. Requires value != null.
        Permission r = Permission.loadPermission("p", data("DV","C0","E0"), PermissionDefault.FALSE, null);
        assertEquals(PermissionDefault.OP, r.getDefault());
    }

    // ===================================================================
    // variable: childrenNode  (d1 = data.get("children") at 297)
    // ===================================================================

    @Test
    public void allDuPaths_childrenNode_d1_to_298() {
        // 1 path: 297 -> 298 (the instanceof check is the use).
        Permission r = Permission.loadPermission("p", data("D0","CIe","E0"), PermissionDefault.OP, null);
        assertNotNull(r);
    }

    @Test
    public void allDuPaths_childrenNode_d1_to_300() {
        // 1 path: 297 -> 298(true: Iterable) -> 299 -> 300 (the cast at the
        // for-each header uses childrenNode).
        Permission r = Permission.loadPermission("p", data("D0","CIe","E0"), PermissionDefault.OP, null);
        assertNotNull(r);
    }

    @Test
    public void allDuPaths_childrenNode_d1_to_306() {
        // 1 path: 297 -> 298(false) -> 305(true: Map) -> 306.
        Permission r = Permission.loadPermission("p", data("D0","CM","E0"), PermissionDefault.OP, null);
        assertEquals(Boolean.TRUE, r.getChildren().get("kid"));
    }

    // ===================================================================
    // variable: child  (d1 = loop variable at 300)
    // ===================================================================

    @Test
    public void allDuPaths_child_d1_to_301() {
        // 1 path: a single loop iteration reaches the null-check at 301. Use
        // CIn (null element) so the false branch is taken and 302 is NOT
        // executed -- isolating the (d1, 301) pair.
        Permission r = Permission.loadPermission("p", data("D0","CIn","E0"), PermissionDefault.OP, null);
        assertEquals(0, r.getChildren().size());
    }

    @Test
    public void allDuPaths_child_d1_to_302() {
        // 1 path: 301(true) -> 302. Requires non-null child element.
        Permission r = Permission.loadPermission("p", data("D0","CIv","E0"), PermissionDefault.OP, null);
        assertEquals(Boolean.TRUE, r.getChildren().get("kid"));
    }
}
