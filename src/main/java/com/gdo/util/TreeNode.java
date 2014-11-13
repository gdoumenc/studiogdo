/**
 * <p>Common tree node structure.<p>
 *
 * <blockquote>
 * <p>&copy; 2004, 2008 StudioGdo/Guillaume Doumenc. All Rights Reserved.
 * This software is the proprietary information of StudioGdo &amp; Guillaume Doumenc.
 * Use is subject to license terms.</p>
 * </blockquote>
 * 
 * @author Guillaume Doumenc (<a> href="mailto:gdoumenc@studiogdo.com">gdoumenc@studiogdo.com)</a>
 */
package com.gdo.util;

import java.util.ArrayList;
import java.util.List;

public abstract class TreeNode<E extends TreeNode<E>> {
    private static int ID_COUNTER = 0; // global node counter

    protected String _id;

    protected E _parent; // parent component
    protected List<E> _children; // sub components

    public String getId() {
        return _id;
    }

    public void setId(int id) {
        _id = Integer.toString(id);
    }

    public void setId(String id) {
        _id = id;
    }

    public E getParent() {
        return _parent;
    }

    public boolean hasChildren() {
        return (_children != null && _children.size() > 0);
    }

    public List<E> getChildren() {
        return _children;
    }

    @SuppressWarnings("unchecked")
    public void addChild(E child) {
        if (_children == null)
            _children = new ArrayList<E>();
        _children.add(child);
        child._parent = (E) this;
    }

    public void addNewChild(E child) {
        child.setId(ID_COUNTER++);
        addChild(child);
    }

    public void addNewChild(E component, E child) {
        child.setId(ID_COUNTER++);
        component.addChild(child);
    }

}
