package org.activehome.context.helper;

import org.activehome.context.data.Device;
import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.TypeDefinition;
import org.kevoree.Value;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class for common Kevoree model manipulation.
 *
 * @author Jacky Bourgeois
 * @version %I%, %G%
 */
public final class ModelHelper {

    /**
     * Utility class.
     */
    private ModelHelper() {
    }

    /**
     * @param type          The type of component (class)
     * @param nodeNameArray List of nodes to look at
     * @param localModel    The Kevoree model to look at
     * @return A list of node name found
     */
    public static LinkedList<String> findAllRunning(
            final String type,
            final String[] nodeNameArray,
            final ContainerRoot localModel) {
        LinkedList<String> list = new LinkedList<>();
        if (localModel != null) {
            for (String nodeName : nodeNameArray) {
                ContainerNode node = localModel.findNodesByID(nodeName);
                if (node != null) {
                    node.getComponents().stream()
                            .filter(ci -> ci.getTypeDefinition() != null
                                    && ci.getTypeDefinition().getName() != null)
                            .forEach(ci -> {
                                if (ci.getTypeDefinition() != null) {
                                    if (ci.getTypeDefinition().getName() != null
                                            && ci.getTypeDefinition().getName().compareTo(type) == 0) {
                                        list.add(nodeName + "." + ci.getName());
                                    } else if (lookForSuperType(ci.getTypeDefinition().getSuperTypes(), type)) {
                                        list.add(nodeName + "." + ci.getName());
                                    }
                                }
                            });
                }
            }
        }
        return list;
    }

    /**
     * @param type          The type of component (class)
     * @param nodeNameArray List of nodes to look at
     * @param localModel    The Kevoree model to look at
     * @return A map of &lt;name, Device&gt; found
     */
    public static HashMap<String, Device> findAllRunningDevice(
            final String type,
            final String[] nodeNameArray,
            final ContainerRoot localModel) {
        HashMap<String, Device> map = new HashMap<>();
        if (localModel != null) {
            for (String nodeName : nodeNameArray) {
                ContainerNode node = localModel.findNodesByID(nodeName);
                if (node != null) {
                    map.putAll(findDeviceInNode(node, type));
                }
            }
        }
        return map;
    }

    /**
     * @param node The node to look at
     * @param type The type of component (class)
     * @return A map of &lt;name, Device&gt; found
     */
    private static HashMap<String, Device> findDeviceInNode(
            final ContainerNode node,
            final String type) {
        HashMap<String, Device> map = new HashMap<>();
        node.getComponents().stream()
                .filter(ci -> ci.getTypeDefinition() != null && ci.getTypeDefinition().getName() != null)
                .forEach(ci -> {
                    if (ci.getTypeDefinition() != null && ci.getTypeDefinition().getName() != null) {
                        String typeName = ci.getTypeDefinition().getName();
                        if (ci.getTypeDefinition().getName().equals(type)
                                || lookForSuperType(ci.getTypeDefinition().getSuperTypes(), type)) {
                            String id = node.getName() + "." + ci.getName();
                            Device device = new Device(ci.getName(), id, typeName, getComponentAttributes(ci));
                            map.put(id, device);
                        }
                    }
                });
        return map;
    }

    /**
     * Recursively check if a super type of a TypeDefinition
     * is of given type 'superType'.
     *
     * @param tdList    The list of super types
     * @param superType The type we are looking for
     * @return true if superType is found
     */
    private static boolean lookForSuperType(final List<TypeDefinition> tdList,
                                            final String superType) {
        for (TypeDefinition td : tdList) {
            if (td.getName() != null
                    && td.getName().compareTo(superType) == 0) {
                return true;
            } else if (lookForSuperType(td.getSuperTypes(), superType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param ci The component instance we look at
     * @return The map &lt;attribute,value&gt; of the given component instance
     */
    public static HashMap<String, String> getComponentAttributes(
            final ComponentInstance ci) {
        HashMap<String, String> attrMap = new HashMap<>();
        if (ci.getDictionary() != null) {
            for (Value value : ci.getDictionary().getValues()) {
                attrMap.put(value.getName(), value.getValue());
            }
        }
        return attrMap;
    }

}
