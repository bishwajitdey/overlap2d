/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package com.uwsoft.editor.mvc.view.ui.box;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.uwsoft.editor.mvc.Overlap2DFacade;
import com.uwsoft.editor.renderer.data.LayerItemVO;


/**
 * Created by azakhary on 4/17/2015.
 */
public class UILayerBox extends UICollapsibleBox {

    public static final String LAYER_ROW_CLICKED = "com.uwsoft.editor.mvc.view.ui.box.UILayerBox" + ".LAYER_ROW_CLICKED";
    public static final String CREATE_NEW_LAYER = "com.uwsoft.editor.mvc.view.ui.box.UILayerBox" + ".CREATE_NEW_LAYER";
    public static final String DELETE_NEW_LAYER = "com.uwsoft.editor.mvc.view.ui.box.UILayerBox" + ".DELETE_NEW_LAYER";
    private final DragAndDrop dragAndDrop;
    public int currentSelectedLayerIndex = 0;
    private Overlap2DFacade facade;
    private VisTable contentTable;
    private VisTable bottomPane;
    private VisScrollPane scrollPane;
    private VisTable layersTable;
    private Array<UILayerItemSlot> rows = new Array<>();

    public UILayerBox() {
        super("Layers", 222);

        facade = Overlap2DFacade.getInstance();

        setMovable(false);
        contentTable = new VisTable();

        layersTable = new VisTable();
        scrollPane = new VisScrollPane(layersTable);
        scrollPane.setFadeScrollBars(false);
        contentTable.add(scrollPane).width(230).height(150);

        scrollPane.layout();

        bottomPane = new VisTable();

        VisTextButton newBtn = new VisTextButton("new");
        VisTextButton deleteBtn = new VisTextButton("delete");
        bottomPane.add(newBtn);
        bottomPane.add(deleteBtn);

        newBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                facade.sendNotification(CREATE_NEW_LAYER);
            }
        });

        deleteBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                facade.sendNotification(DELETE_NEW_LAYER);
            }
        });
        dragAndDrop = new DragAndDrop();
//        contentTable.add(bottomPane);
        createCollapsibleWidget(contentTable);
    }

    public int getCurrentSelectedLayerIndex() {
        return currentSelectedLayerIndex;
    }

    public void clearItems() {
        layersTable.clear();
        rows.clear();
    }

    public void addItem(LayerItemVO itemVO) {
        UILayerItemSlot itemSlot = new UILayerItemSlot();
        UILayerItem item = new UILayerItem(itemVO, itemSlot);
        layersTable.add(itemSlot).left().expandX().fillX();
        layersTable.row().padTop(1);
        dragAndDrop.addSource(new SlotSource(item));
        dragAndDrop.addTarget(new SlotTarget(itemSlot));
        dragAndDrop.setDragActorPosition(0, 0);
        rows.add(itemSlot);

        itemSlot.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                for (int i = 0; i < rows.size; i++) {
                    if (i != rows.indexOf(itemSlot, true)) {
                        rows.get(i).getUiLayerItem().setSelected(false);
                    }
                }
                itemSlot.getUiLayerItem().setSelected(true);
                currentSelectedLayerIndex = rows.indexOf(itemSlot, true);

                facade.sendNotification(LAYER_ROW_CLICKED, itemSlot.getUiLayerItem());
            }
        });
    }

    private static class SlotSource extends DragAndDrop.Source {


        public SlotSource(UILayerItem item) {
            super(item);
        }

        @Override
        public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
            DragAndDrop.Payload payload = new DragAndDrop.Payload();
            payload.setDragActor(new UILayerItemDragActor((UILayerItem) getActor()));
            return payload;
        }

        @Override
        public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
            UILayerItem uiLayerItemActor = (UILayerItem) getActor();
            UILayerItemSlot uiLayerItemSlot = uiLayerItemActor.getItemSlot();
            if (target != null) {
                UILayerItemSlot uiLayerItemSlotTarget = (UILayerItemSlot) target.getActor();
                UILayerItem uiLayerItemTarget = uiLayerItemSlotTarget.getUiLayerItem();
                //
                uiLayerItemActor.setItemSlot(uiLayerItemSlotTarget);
                uiLayerItemTarget.setItemSlot(uiLayerItemSlot);

            } else {
                uiLayerItemActor.setItemSlot(uiLayerItemSlot);
            }
        }
    }

    private static class SlotTarget extends DragAndDrop.Target {

//        private Slot targetSlot;

        public SlotTarget(UILayerItemSlot item) {
            super(item);
        }

        @Override
        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
//            Slot payloadSlot = (Slot) payload.getObject();
//            // if (targetSlot.getItem() == payloadSlot.getItem() ||
//            // targetSlot.getItem() == null) {
//            getActor().setColor(Color.BLUE);
            return true;
            // } else {
            // getActor().setColor(Color.DARK_GRAY);
            // return false;
            // }
        }

        @Override
        public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        }

        @Override
        public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload) {
//            getActor().setColor(Color.LIGHT_GRAY);
        }

    }

    public static class UILayerItemDragActor extends VisTable {
        public UILayerItemDragActor(UILayerItem actor) {
            setWidth(actor.getWidth());
            setHeight(actor.getPrefHeight());
            VisImageButton lockBtn = new VisImageButton("layer-lock");
            VisImageButton visibleBtn = new VisImageButton("layer-visible");
            add(lockBtn).left();
            add(visibleBtn).left().padRight(6);
            add(actor.getLayerName()).expandX().fillX();
            setBackground(VisUI.getSkin().getDrawable(actor.isSelected() ? "layer-bg-over" : "layer-bg"));
            getColor().a = .9f;
        }
    }

    public class UILayerItemSlot extends VisTable {
        private final Cell cell;
        private final Drawable normalBg;
        private final Drawable selectedBg;
        private UILayerItem uiLayerItem;

        public UILayerItemSlot() {
            normalBg = VisUI.getSkin().getDrawable("layer-bg");
            selectedBg = VisUI.getSkin().getDrawable("layer-bg-over");
            setBackground(normalBg);
            cell = add().expandX().fillX();
        }

        private UILayerItemSlot(UILayerItemSlot uiLayerItemSlot) {
            this();
            cell.width(uiLayerItemSlot.cell.getPrefWidth());
            cell.height(uiLayerItemSlot.cell.getPrefHeight());
        }

        public void setLayerItem(UILayerItem uiLayerItem) {
            this.uiLayerItem = uiLayerItem;
            cell.setActor(uiLayerItem);
            cell.height(uiLayerItem.getHeight());
        }

        public UILayerItem getUiLayerItem() {
            return uiLayerItem;
        }

        public void setSelected(boolean selected) {
            setBackground(selected ? selectedBg : normalBg);
        }

        @Override
        protected UILayerItemSlot clone() throws CloneNotSupportedException {
            return new UILayerItemSlot(this);
        }
    }

    public class UILayerItem extends VisTable {


        private final LayerItemVO layerData;
        private UILayerItemSlot itemSlot;
        private boolean selected;

        public UILayerItem(LayerItemVO layerData, UILayerItemSlot itemSlot) {
            super();
            this.layerData = layerData;
            this.itemSlot = itemSlot;
            VisImageButton lockBtn = new VisImageButton("layer-lock");
            lockBtn.addListener(new LockClickListener());
            VisImageButton visibleBtn = new VisImageButton("layer-visible");
            visibleBtn.addListener(new VisibleClickListener());
            add(lockBtn).left();
            add(visibleBtn).left().padRight(6);
            add(layerData.layerName).expandX().fillX();
            //
            itemSlot.setLayerItem(this);
        }

        public boolean isLocked() {
            return layerData.isLocked;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            itemSlot.setSelected(selected);
        }

        public boolean isLayerVisible() {
            return layerData.isVisible;
        }

        public String getLayerName() {
            return layerData.layerName;
        }

        public UILayerItemSlot getItemSlot() {
            return itemSlot;
        }

        public void setItemSlot(UILayerItemSlot itemSlot) {
            this.itemSlot = itemSlot;
            itemSlot.setLayerItem(this);
            itemSlot.setSelected(selected);
        }

        private class LockClickListener extends ClickListener {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                layerData.isLocked = !layerData.isLocked;
            }
        }

        private class VisibleClickListener extends ClickListener {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                layerData.isVisible = !layerData.isVisible;
            }
        }


    }

}
