package org.bukkit.permissions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
 * Criterion: all-defs. For every definition of every variable in the method,
 * at least one definition-clear path to some use of that definition is exercised.
 *
 * Variables and their defs/uses (lines refer to Permission.java):
 *   1. name         d1: param (280)                                   u: 281, 306-as-arg, 316
 *   2. data         d1: param (280)                                   u: 282, 287, 288, 296, 297, 312, 313
 *   3. def          d1: param (280), d2: reassign (290)               u: 306, 316
 *   4. output       d1: param (280)                                   u: 306
 *   5. desc         d1: =null (284), d2: =data.get("description") (313)  u: 316
 *   6. children     d1: =null (285), d2: =new LinkedHashMap (299),
 *                   d3: =extractChildren(...) (306)                   u: 302, 316
 *   7. value        d1: =PermissionDefault.getByName(...) (288)        u: 289, 290
 *   8. childrenNode d1: =data.get("children") (297)                    u: 298, 300, 306
 *   9. child        d1: loop var (300)                                 u: 301, 302
 */
public class LoadPermissionAllDefsTest {

    @BeforeClass
    public static void setUp() {
        TestServer.getInstance();
    }

    // ---------- variable: def ----------

    @Test
    public void testAllDefs_def_d1_reachesU2() {
        Map<String, Object> data = new HashMap<String, Object>();

        Permission result = Permission.loadPermission(
                "test.perm.def.d1", data, PermissionDefault.TRUE, null);

        assertThat(result.getDefault(), is(PermissionDefault.TRUE));
    }

    @Test
    public void testAllDefs_def_d2_reachesU2() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("default", "OP");

        Permission result = Permission.loadPermission(
                "test.perm.def.d2", data, PermissionDefault.FALSE, null);

        assertThat(result.getDefault(), is(PermissionDefault.OP));
    }

    // ---------- variable: name ----------
    // Only one def (parameter). A simple call exercises uses at line 281 and 316.

    @Test
    public void testAllDefs_name_d1_reachesUse() {
        Map<String, Object> data = new HashMap<String, Object>();

        Permission result = Permission.loadPermission(
                "test.perm.name.d1", data, PermissionDefault.FALSE, null);

        assertThat(result.getName(), is("test.perm.name.d1"));
    }

    // ---------- variable: data ----------
    // Only one def (parameter). Any call hits uses (data.get("default"), etc.).

    @Test
    public void testAllDefs_data_d1_reachesUse() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("description", "hello");

        Permission result = Permission.loadPermission(
                "test.perm.data.d1", data, PermissionDefault.FALSE, null);

        assertThat(result.getDescription(), is("hello"));
    }

    // ---------- variable: output ----------
    // Only one def (parameter). Its only use is the extractChildren call at 306,
    // reached when "children" is a Map. extractChildren appends to output when
    // a nested permission is loaded — we assert output received the nested perm.

    @Test
    public void testAllDefs_output_d1_reachesUse() {
        Map<String, Object> nestedChild = new HashMap<String, Object>();
        nestedChild.put("description", "nested");

        Map<String, Object> children = new LinkedHashMap<String, Object>();
        children.put("test.perm.output.nested", nestedChild);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", children);

        List<Permission> output = new ArrayList<Permission>();
        Permission result = Permission.loadPermission(
                "test.perm.output.d1", data, PermissionDefault.FALSE, output);

        assertThat(result, is(notNullValue()));
        assertTrue("output should have received the nested permission",
                output.size() == 1
                && "test.perm.output.nested".equals(output.get(0).getName()));
    }

    // ---------- variable: desc ----------

    @Test
    public void testAllDefs_desc_d1_nullReachesU() {
        // d1: desc = null at 284 reaches the constructor at 316 when no
        // "description" key is present (skipping 312, so d2 does not fire).
        Map<String, Object> data = new HashMap<String, Object>();

        Permission result = Permission.loadPermission(
                "test.perm.desc.d1", data, PermissionDefault.FALSE, null);

        // Permission ctor converts null description to "".
        assertThat(result.getDescription(), is(""));
    }

    @Test
    public void testAllDefs_desc_d2_reachesU() {
        // d2: desc = data.get("description").toString() at 313 reaches 316.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("description", "my-desc");

        Permission result = Permission.loadPermission(
                "test.perm.desc.d2", data, PermissionDefault.FALSE, null);

        assertThat(result.getDescription(), is("my-desc"));
    }

    // ---------- variable: children ----------

    @Test
    public void testAllDefs_children_d1_nullReachesU() {
        // d1: children = null at 285 reaches 316 when "children" key absent.
        Map<String, Object> data = new HashMap<String, Object>();

        Permission result = Permission.loadPermission(
                "test.perm.children.d1", data, PermissionDefault.FALSE, null);

        assertThat(result.getChildren().isEmpty(), is(true));
    }

    @Test
    public void testAllDefs_children_d2_iterableReachesU() {
        // d2: children = new LinkedHashMap at 299, then children.put at 302
        // (use) inside the Iterable branch. Reaches 316 as well.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Arrays.asList("test.perm.children.iter.kid"));

        Permission result = Permission.loadPermission(
                "test.perm.children.d2", data, PermissionDefault.FALSE, null);

        assertThat(result.getChildren().get("test.perm.children.iter.kid"),
                is(Boolean.TRUE));
    }

    @Test
    public void testAllDefs_children_d3_extractChildrenReachesU() {
        // d3: children = extractChildren(...) at 306 reaches 316.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Collections.singletonMap(
                "test.perm.children.map.kid", Boolean.TRUE));

        Permission result = Permission.loadPermission(
                "test.perm.children.d3", data, PermissionDefault.FALSE, null);

        assertThat(result.getChildren().get("test.perm.children.map.kid"),
                is(Boolean.TRUE));
    }

    // ---------- variable: value ----------
    // Single def at 288 (PermissionDefault.getByName). Its uses are the null
    // check at 289 and "def = value" at 290. Both require entering the
    // "default" branch with a valid PermissionDefault name.

    @Test
    public void testAllDefs_value_d1_reachesUse() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("default", "TRUE");

        Permission result = Permission.loadPermission(
                "test.perm.value.d1", data, PermissionDefault.FALSE, null);

        // If value reached its uses (non-null check + def assignment),
        // returned default reflects the parsed value.
        assertThat(result.getDefault(), is(PermissionDefault.TRUE));
    }

    // ---------- variable: childrenNode ----------
    // Single def at 297. Use at 298 (instanceof Iterable). Any "children"
    // value reaches the use.

    @Test
    public void testAllDefs_childrenNode_d1_reachesUse() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Collections.emptyList());

        Permission result = Permission.loadPermission(
                "test.perm.childrenNode.d1", data, PermissionDefault.FALSE, null);

        // Reaching the use means instanceof check ran; empty iterable produces
        // an empty children map.
        assertThat(result.getChildren().isEmpty(), is(true));
    }

    // ---------- variable: child ----------
    // Single def (loop variable at 300). Uses at 301 (null check) and 302
    // (children.put). Requires a non-empty Iterable so the loop body executes.

    @Test
    public void testAllDefs_child_d1_reachesUse() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("children", Arrays.asList("test.perm.child.kid"));

        Permission result = Permission.loadPermission(
                "test.perm.child.d1", data, PermissionDefault.FALSE, null);

        assertThat(result.getChildren().get("test.perm.child.kid"),
                is(Boolean.TRUE));
    }
}
