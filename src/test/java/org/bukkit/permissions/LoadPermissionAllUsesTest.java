package org.bukkit.permissions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
 * Criterion: all-uses. For every (definition d, use u) where a def-clear path
 * from d to u exists, exercise at least one such path.
 *
 * Test naming: testAllUses_<var>_<defId>_to_line<N>_<what>.
 *
 * Note on excluded pairs (no def-clear path):
 *   - children d1 (null at 285) -> 302: killed by d2 (=new LinkedHashMap at 299)
 *     before any execution of 302 (302 lives inside the Iterable branch where d2
 *     always fires first).
 *   - children d2 (line 299) -> 316: still reachable; included.
 *   - children d3 (line 306) -> 302: 302 is in the Iterable branch, 306 is in the
 *     Map branch -> mutually exclusive.
 */
public class LoadPermissionAllUsesTest {

    @BeforeClass
    public static void setUp() {
        TestServer.getInstance();
    }

    // ============================================================
    // variable: name (d1 = parameter)
    // uses: 281 Validate.notNull, 306 extractChildren arg, 316 ctor arg
    // ============================================================

    @Test
    public void testAllUses_name_d1_to_line281_Validate() {
        // Any non-null name exercises Validate.notNull(name, ...).
        Permission result = Permission.loadPermission(
                "test.name.281", new HashMap<String, Object>(),
                PermissionDefault.FALSE, null);
        assertThat(result.getName(), is("test.name.281"));
    }

    @Test
    public void testAllUses_name_d1_to_line306_extractChildrenArg() {
        // children-as-Map path takes the call site at 306 which uses `name`.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Collections.singletonMap("test.name.306.kid", Boolean.TRUE));
        Permission result = Permission.loadPermission(
                "test.name.306", data, PermissionDefault.FALSE, null);
        assertThat(result.getName(), is("test.name.306"));
    }

    @Test
    public void testAllUses_name_d1_to_line316_ctor() {
        Permission result = Permission.loadPermission(
                "test.name.316", new HashMap<String, Object>(),
                PermissionDefault.FALSE, null);
        assertThat(result.getName(), is("test.name.316"));
    }

    // ============================================================
    // variable: data (d1 = parameter)
    // uses: 282, 287, 288, 296, 297, 312, 313
    // ============================================================

    @Test
    public void testAllUses_data_d1_to_line282_Validate() {
        // A non-null data passes Validate.notNull(data, ...).
        Permission result = Permission.loadPermission(
                "test.data.282", new HashMap<String, Object>(),
                PermissionDefault.FALSE, null);
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void testAllUses_data_d1_to_line287_defaultIfCheck() {
        // data.get("default") evaluated -> the if condition at 287.
        Permission result = Permission.loadPermission(
                "test.data.287", new HashMap<String, Object>(),
                PermissionDefault.TRUE, null);
        // No "default" key -> condition false -> def parameter survives.
        assertThat(result.getDefault(), is(PermissionDefault.TRUE));
    }

    @Test
    public void testAllUses_data_d1_to_line288_defaultGetByName() {
        // Use at 288 (inside the "default" branch).
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("default", "OP");
        Permission result = Permission.loadPermission(
                "test.data.288", data, PermissionDefault.FALSE, null);
        assertThat(result.getDefault(), is(PermissionDefault.OP));
    }

    @Test
    public void testAllUses_data_d1_to_line296_childrenIfCheck() {
        // data.get("children") evaluated -> the if condition at 296.
        Permission result = Permission.loadPermission(
                "test.data.296", new HashMap<String, Object>(),
                PermissionDefault.FALSE, null);
        assertThat(result.getChildren().isEmpty(), is(true));
    }

    @Test
    public void testAllUses_data_d1_to_line297_childrenGet() {
        // Use at 297 inside the "children" branch.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Collections.emptyList());
        Permission result = Permission.loadPermission(
                "test.data.297", data, PermissionDefault.FALSE, null);
        assertThat(result.getChildren().isEmpty(), is(true));
    }

    @Test
    public void testAllUses_data_d1_to_line312_descriptionIfCheck() {
        // data.get("description") evaluated -> if condition at 312.
        Permission result = Permission.loadPermission(
                "test.data.312", new HashMap<String, Object>(),
                PermissionDefault.FALSE, null);
        assertThat(result.getDescription(), is(""));
    }

    @Test
    public void testAllUses_data_d1_to_line313_descriptionGet() {
        // Use at 313 inside the "description" branch.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("description", "hello");
        Permission result = Permission.loadPermission(
                "test.data.313", data, PermissionDefault.FALSE, null);
        assertThat(result.getDescription(), is("hello"));
    }

    // ============================================================
    // variable: def (d1 = parameter, d2 = reassignment at 290)
    // uses: 306 extractChildren arg, 316 ctor arg
    // ============================================================

    @Test
    public void testAllUses_def_d1_to_line306_extractChildrenArg() {
        // No "default" key (d1 not killed) AND children-as-Map (use at 306).
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Collections.singletonMap("test.def.d1.306.kid", Boolean.TRUE));
        Permission result = Permission.loadPermission(
                "test.def.d1.306", data, PermissionDefault.TRUE, null);
        // Parameter def survived to 306 -> also to 316.
        assertThat(result.getDefault(), is(PermissionDefault.TRUE));
    }

    @Test
    public void testAllUses_def_d1_to_line316_ctorArg() {
        // No "default" key -> d1 reaches 316.
        Permission result = Permission.loadPermission(
                "test.def.d1.316", new HashMap<String, Object>(),
                PermissionDefault.NOT_OP, null);
        assertThat(result.getDefault(), is(PermissionDefault.NOT_OP));
    }

    @Test
    public void testAllUses_def_d2_to_line306_extractChildrenArg() {
        // "default" valid (d2 fires) AND children-as-Map.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("default", "OP");
        data.put("children", Collections.singletonMap("test.def.d2.306.kid", Boolean.TRUE));
        Permission result = Permission.loadPermission(
                "test.def.d2.306", data, PermissionDefault.FALSE, null);
        // d2's value (OP), not the parameter (FALSE), reaches the use.
        assertThat(result.getDefault(), is(PermissionDefault.OP));
    }

    @Test
    public void testAllUses_def_d2_to_line316_ctorArg() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("default", "OP");
        Permission result = Permission.loadPermission(
                "test.def.d2.316", data, PermissionDefault.FALSE, null);
        assertThat(result.getDefault(), is(PermissionDefault.OP));
    }

    // ============================================================
    // variable: output (d1 = parameter)
    // uses: 306 extractChildren arg
    // ============================================================

    @Test
    public void testAllUses_output_d1_to_line306_extractChildrenArg() {
        // children-as-Map path with a nested child -> extractChildren appends
        // to output, proving the parameter reached the use site.
        Map<String, Object> nested = new HashMap<String, Object>();
        nested.put("description", "n");
        Map<String, Object> children = new LinkedHashMap<String, Object>();
        children.put("test.output.306.nested", nested);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", children);

        List<Permission> output = new ArrayList<Permission>();
        Permission.loadPermission(
                "test.output.306", data, PermissionDefault.FALSE, output);

        assertTrue(output.size() == 1
                && "test.output.306.nested".equals(output.get(0).getName()));
    }

    // ============================================================
    // variable: desc (d1 = null at 284, d2 = data.get("description") at 313)
    // uses: 316 ctor arg
    // ============================================================

    @Test
    public void testAllUses_desc_d1_to_line316_ctorArg() {
        // No "description" key -> d2 doesn't fire, d1 (null) reaches 316.
        Permission result = Permission.loadPermission(
                "test.desc.d1.316", new HashMap<String, Object>(),
                PermissionDefault.FALSE, null);
        // ctor converts null description to "".
        assertThat(result.getDescription(), is(""));
    }

    @Test
    public void testAllUses_desc_d2_to_line316_ctorArg() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("description", "hi");
        Permission result = Permission.loadPermission(
                "test.desc.d2.316", data, PermissionDefault.FALSE, null);
        assertThat(result.getDescription(), is("hi"));
    }

    // ============================================================
    // variable: children
    //   d1 = null at 285
    //   d2 = new LinkedHashMap at 299
    //   d3 = extractChildren(...) at 306
    // uses: 302 children.put, 316 ctor arg
    // ============================================================

    @Test
    public void testAllUses_children_d1_to_line316_ctorArg() {
        // No "children" key -> d1 (null) reaches 316.
        Permission result = Permission.loadPermission(
                "test.children.d1.316", new HashMap<String, Object>(),
                PermissionDefault.FALSE, null);
        assertThat(result.getChildren().isEmpty(), is(true));
    }

    @Test
    public void testAllUses_children_d2_to_line302_put() {
        // Iterable branch with a non-null element -> 302 executes.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Arrays.asList("test.children.d2.302.kid"));
        Permission result = Permission.loadPermission(
                "test.children.d2.302", data, PermissionDefault.FALSE, null);
        assertThat(result.getChildren().get("test.children.d2.302.kid"),
                is(Boolean.TRUE));
    }

    @Test
    public void testAllUses_children_d2_to_line316_ctorArg() {
        // Iterable branch -> d2 fires -> reaches 316.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Collections.emptyList());
        Permission result = Permission.loadPermission(
                "test.children.d2.316", data, PermissionDefault.FALSE, null);
        // Empty iterable -> empty map at 316.
        assertThat(result.getChildren().isEmpty(), is(true));
    }

    @Test
    public void testAllUses_children_d3_to_line316_ctorArg() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Collections.singletonMap("test.children.d3.316.kid", Boolean.TRUE));
        Permission result = Permission.loadPermission(
                "test.children.d3.316", data, PermissionDefault.FALSE, null);
        assertThat(result.getChildren().get("test.children.d3.316.kid"),
                is(Boolean.TRUE));
    }

    // ============================================================
    // variable: value (d1 = PermissionDefault.getByName(...) at 288)
    // uses: 289 null check, 290 def = value
    // ============================================================

    @Test
    public void testAllUses_value_d1_to_line289_nullCheck() {
        // Invalid name -> value is null -> null-check at 289 runs (and fails),
        // triggering IllegalArgumentException at 292. The use at 289 IS the
        // null comparison, and it executes.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("default", "NOT_A_REAL_VALUE");
        try {
            Permission.loadPermission(
                    "test.value.d1.289", data, PermissionDefault.FALSE, null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Reaching the throw proves the null check at 289 ran.
        }
    }

    @Test
    public void testAllUses_value_d1_to_line290_defAssign() {
        // Valid name -> value != null -> assignment at 290 executes.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("default", "OP");
        Permission result = Permission.loadPermission(
                "test.value.d1.290", data, PermissionDefault.FALSE, null);
        // The assignment reached 290; OP propagates to the returned permission.
        assertThat(result.getDefault(), is(PermissionDefault.OP));
    }

    // ============================================================
    // variable: childrenNode (d1 = data.get("children") at 297)
    // uses: 298 instanceof Iterable, 300 (Iterable<?>) cast, 306 (Map<?,?>) cast
    // ============================================================

    @Test
    public void testAllUses_childrenNode_d1_to_line298_instanceofIterable() {
        // Any "children" value reaches the instanceof check at 298.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Collections.emptyList());
        Permission result = Permission.loadPermission(
                "test.childrenNode.d1.298", data, PermissionDefault.FALSE, null);
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void testAllUses_childrenNode_d1_to_line300_iterableCast() {
        // Iterable value -> cast at 300 uses childrenNode.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Arrays.asList("test.childrenNode.d1.300.kid"));
        Permission result = Permission.loadPermission(
                "test.childrenNode.d1.300", data, PermissionDefault.FALSE, null);
        assertThat(result.getChildren().get("test.childrenNode.d1.300.kid"),
                is(Boolean.TRUE));
    }

    @Test
    public void testAllUses_childrenNode_d1_to_line306_mapCastForExtract() {
        // Map value -> cast at 306 uses childrenNode.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Collections.singletonMap("test.childrenNode.d1.306.kid", Boolean.TRUE));
        Permission result = Permission.loadPermission(
                "test.childrenNode.d1.306", data, PermissionDefault.FALSE, null);
        assertThat(result.getChildren().get("test.childrenNode.d1.306.kid"),
                is(Boolean.TRUE));
    }

    // ============================================================
    // variable: child (d1 = loop variable at 300)
    // uses: 301 null check, 302 children.put(child.toString(), ...)
    // ============================================================

    @Test
    public void testAllUses_child_d1_to_line301_nullCheck() {
        // A null element exercises the null check (which returns false, so 302
        // does NOT execute). Pair (d1, 301) is satisfied independently.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Collections.singletonList(null));
        Permission result = Permission.loadPermission(
                "test.child.d1.301", data, PermissionDefault.FALSE, null);
        // Null was filtered, so children map is empty.
        assertThat(result.getChildren().isEmpty(), is(true));
    }

    @Test
    public void testAllUses_child_d1_to_line302_put() {
        // A non-null element exercises children.put(child.toString(), TRUE).
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Arrays.asList("test.child.d1.302.kid"));
        Permission result = Permission.loadPermission(
                "test.child.d1.302", data, PermissionDefault.FALSE, null);
        assertThat(result.getChildren().get("test.child.d1.302.kid"),
                is(Boolean.TRUE));
    }
}
