package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.IHudRenderer;

import java.util.*;

/**
 * Renders and handles interaction for the left sidebar in the HUD Editor.
 * Supports three navigation tabs:
 * 1. COMPONENTS - Component catalog, category filtering, search, and visibility toggles.
 * 2. LAYERS     - Z-Index rendering layers, visibility/lock toggling, and elevation.
 * 3. GROUPS     - Logical component groupings, multi-selection activation, and ungrouping.
 */
public class HudEditorComponentList {

    private int x;
    private int y;
    private int width;
    private int height;

    private final List<HudComponentDescriptor> filteredDescriptors = new ArrayList<>();
    private int maxScroll = 0;

    public void updateBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(IHudRenderer renderer, HudLayout layout, HudEditorState state,
                       HudSelectionManager selectionManager, int mouseX, int mouseY) {
        if (state.isLeftPanelCollapsed()) {
            renderCollapsedStrip(renderer, mouseX, mouseY);
            return;
        }

        // Panel background & right border
        renderer.fill(x, y, width, height, 0xEE16181F);
        renderer.fill(x + width - 1, y, 1, height, 0xFF2A2E3D);

        // Header
        renderer.drawText("PANEL BOCZNY", x + 12, y + 10, 0xFFE0E0E0, true, 0.9f, true);

        // Collapse Button
        int collapseBtnX = x + width - 24;
        int collapseBtnY = y + 8;
        boolean collapseHover = mouseX >= collapseBtnX && mouseX <= collapseBtnX + 16 && mouseY >= collapseBtnY && mouseY <= collapseBtnY + 16;
        renderer.fill(collapseBtnX, collapseBtnY, 16, 16, collapseHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawText("◀", collapseBtnX + 4, collapseBtnY + 4, 0xFFCCCCCC, false, 0.8f, false);

        // Navigation Tabs: [Komponenty] [Warstwy] [Grupy]
        int tabY = y + 28;
        int tabH = 18;
        int tabW = (width - 24) / 3;

        renderTabButton(renderer, x + 10, tabY, tabW, tabH, "Elementy", state.getLeftPanelTab() == HudEditorState.LeftPanelTab.COMPONENTS, mouseX, mouseY);
        renderTabButton(renderer, x + 12 + tabW, tabY, tabW, tabH, "Warstwy", state.getLeftPanelTab() == HudEditorState.LeftPanelTab.LAYERS, mouseX, mouseY);
        renderTabButton(renderer, x + 14 + 2 * tabW, tabY, tabW, tabH, "Grupy", state.getLeftPanelTab() == HudEditorState.LeftPanelTab.GROUPS, mouseX, mouseY);

        // Render Active Tab Content
        switch (state.getLeftPanelTab()) {
            case LAYERS:
                renderLayersTab(renderer, layout, state, mouseX, mouseY);
                break;
            case GROUPS:
                renderGroupsTab(renderer, layout, state, mouseX, mouseY);
                break;
            case COMPONENTS:
            default:
                renderComponentsTab(renderer, layout, state, selectionManager, mouseX, mouseY);
                break;
        }
    }

    private void renderTabButton(IHudRenderer renderer, int bx, int by, int bw, int bh,
                                 String label, boolean isActive, int mouseX, int mouseY) {
        boolean hover = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
        int bg = isActive ? 0xFF4A90E2 : (hover ? 0xFF353A4C : 0xFF232734);
        int border = isActive ? 0xFF88CCFF : 0xFF2A2E3D;
        renderer.fill(bx, by, bw, bh, bg);
        renderer.drawBorder(bx, by, bw, bh, border, 1);
        renderer.drawCenteredTextWithShadow(label, bx + bw / 2, by + 5, isActive ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    private void renderComponentsTab(IHudRenderer renderer, HudLayout layout, HudEditorState state,
                                     HudSelectionManager selectionManager, int mouseX, int mouseY) {
        // Search Input Box
        int searchX = x + 10;
        int searchY = y + 50;
        int searchW = width - 20;
        int searchH = 20;
        boolean searchHover = mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + searchH;
        renderer.fill(searchX, searchY, searchW, searchH, 0xFF12141A);
        int searchBorder = state.isSearching() ? 0xFF55FF55 : (searchHover ? 0xFF4A90E2 : 0xFF2A2E3D);
        renderer.drawBorder(searchX, searchY, searchW, searchH, searchBorder, 1);

        String searchDisplay = state.getSearchQuery().isEmpty()
                ? (state.isSearching() ? "_" : "🔍 Szukaj...")
                : state.getSearchQuery() + (state.isSearching() ? "_" : "");
        int searchTextColor = state.getSearchQuery().isEmpty() && !state.isSearching() ? 0xFF777777 : 0xFFFFFFFF;
        renderer.drawText(searchDisplay, searchX + 6, searchY + 6, searchTextColor, false, 0.8f, false);

        // Category Filter Tabs
        int catY = searchY + 24;
        String[] categories = new String[]{"ALL", "HUD", "PvP", "Survival", "Visual", "Other"};
        int catBtnW = (width - 24) / 3;
        int catBtnH = 16;

        for (int i = 0; i < categories.length; i++) {
            String cat = categories[i];
            int cx = x + 10 + (i % 3) * (catBtnW + 2);
            int cy = catY + (i / 3) * (catBtnH + 3);
            boolean isCatSelected = cat.equalsIgnoreCase(state.getSelectedCategory());
            boolean catHover = mouseX >= cx && mouseX <= cx + catBtnW && mouseY >= cy && mouseY <= cy + catBtnH;

            int catBg = isCatSelected ? 0xFF4A90E2 : (catHover ? 0xFF353A4C : 0xFF232734);
            renderer.fill(cx, cy, catBtnW, catBtnH, catBg);
            renderer.drawCenteredTextWithShadow(cat, cx + catBtnW / 2, cy + 4, isCatSelected ? 0xFFFFFFFF : 0xFFAAAAAA);
        }

        // Filter and update descriptors list
        updateFilteredList(state);

        // Scrollable List of Components
        int listY = catY + 40;
        int listH = height - (listY - y) - 8;
        int itemH = 34;
        int itemGap = 4;
        int totalH = filteredDescriptors.size() * (itemH + itemGap);
        this.maxScroll = Math.max(0, totalH - listH);

        int currentY = listY - state.getListScrollOffset();
        String selectedId = selectionManager.getSelectedComponentId();

        for (HudComponentDescriptor desc : filteredDescriptors) {
            if (currentY + itemH >= listY && currentY <= listY + listH) {
                renderComponentItem(renderer, layout, desc, x + 8, currentY, width - 16, itemH,
                        desc.getId().equalsIgnoreCase(selectedId) || state.getMultiSelectionManager().isSelected(desc.getId()),
                        mouseX, mouseY);
            }
            currentY += itemH + itemGap;
        }
    }

    private void renderLayersTab(IHudRenderer renderer, HudLayout layout, HudEditorState state, int mouseX, int mouseY) {
        int curY = y + 52;
        renderer.drawText("WARSTWY RENDEROWANIA", x + 12, curY, 0xFFE0E0E0, true, 0.85f, true);
        curY += 16;

        List<HudLayer> layers = new ArrayList<>(layout.getLayers().values());
        layers.sort((a, b) -> Integer.compare(b.getZIndexBase(), a.getZIndexBase()));

        int itemH = 36;
        int itemGap = 4;
        int listH = height - (curY - y) - 36;
        int totalH = layers.size() * (itemH + itemGap);
        this.maxScroll = Math.max(0, totalH - listH);

        int drawY = curY - state.getListScrollOffset();
        for (HudLayer layer : layers) {
            if (drawY + itemH >= curY && drawY <= curY + listH) {
                renderLayerItem(renderer, layout, layer, x + 8, drawY, width - 16, itemH, mouseX, mouseY);
            }
            drawY += itemH + itemGap;
        }

        // Add Layer button at bottom
        int addBtnY = y + height - 28;
        int addBtnW = width - 20;
        boolean addHover = mouseX >= x + 10 && mouseX <= x + 10 + addBtnW && mouseY >= addBtnY && mouseY <= addBtnY + 20;
        renderer.fill(x + 10, addBtnY, addBtnW, 20, addHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(x + 10, addBtnY, addBtnW, 20, 0xFF4A90E2, 1);
        renderer.drawCenteredTextWithShadow("+ Dodaj warstwę", x + 10 + addBtnW / 2, addBtnY + 6, 0xFF55FFFF);
    }

    private void renderLayerItem(IHudRenderer renderer, HudLayout layout, HudLayer layer,
                                 int itemX, int itemY, int itemW, int itemH, int mouseX, int mouseY) {
        boolean hover = mouseX >= itemX && mouseX <= itemX + itemW && mouseY >= itemY && mouseY <= itemY + itemH;
        renderer.fill(itemX, itemY, itemW, itemH, hover ? 0xFF222632 : 0xFF1C1E27);
        renderer.drawBorder(itemX, itemY, itemW, itemH, 0xFF2A2E3D, 1);

        // Count components
        int compCount = 0;
        for (HudComponentState st : layout.getComponents().values()) {
            if (layer.getLayerId().equalsIgnoreCase(st.getLayerId())) {
                compCount++;
            }
        }

        // Layer Title & Z-Index Base
        renderer.drawText(layer.getName(), itemX + 8, itemY + 6, 0xFFE0E0E0, true, 0.85f, true);
        String sub = String.format("Z: %d • %d elem.", layer.getZIndexBase(), compCount);
        renderer.drawText(sub, itemX + 8, itemY + 20, 0xFF888888, false, 0.7f, false);

        // Toggles: Eye & Lock
        int toggleY = itemY + 9;
        int eyeX = itemX + itemW - 46;
        boolean eyeHover = mouseX >= eyeX && mouseX <= eyeX + 18 && mouseY >= toggleY && mouseY <= toggleY + 18;
        renderer.fill(eyeX, toggleY, 18, 18, eyeHover ? 0xFF353A4C : 0xFF16181F);
        renderer.drawBorder(eyeX, toggleY, 18, 18, layer.isVisible() ? 0xFF55FF55 : 0xFF555555, 1);
        renderer.drawCenteredTextWithShadow(layer.isVisible() ? "👁" : "—", eyeX + 9, toggleY + 5, layer.isVisible() ? 0xFF55FF55 : 0xFF888888);

        int lockX = itemX + itemW - 24;
        boolean lockHover = mouseX >= lockX && mouseX <= lockX + 18 && mouseY >= toggleY && mouseY <= toggleY + 18;
        renderer.fill(lockX, toggleY, 18, 18, lockHover ? 0xFF353A4C : 0xFF16181F);
        renderer.drawBorder(lockX, toggleY, 18, 18, layer.isLocked() ? 0xFFFF5555 : 0xFF555555, 1);
        renderer.drawCenteredTextWithShadow(layer.isLocked() ? "🔒" : "🔓", lockX + 9, toggleY + 5, layer.isLocked() ? 0xFFFF5555 : 0xFFCCCCCC);
    }

    private void renderGroupsTab(IHudRenderer renderer, HudLayout layout, HudEditorState state, int mouseX, int mouseY) {
        int curY = y + 52;
        renderer.drawText("GRUPY LOGICZNE", x + 12, curY, 0xFFE0E0E0, true, 0.85f, true);
        curY += 16;

        // Group creation from selection if multi-selected
        HudMultiSelectionManager multiSel = state.getMultiSelectionManager();
        if (multiSel.getSelectedCount() > 1) {
            int grpBtnW = width - 20;
            boolean grpHover = mouseX >= x + 10 && mouseX <= x + 10 + grpBtnW && mouseY >= curY && mouseY <= curY + 20;
            renderer.fill(x + 10, curY, grpBtnW, 20, grpHover ? 0xFF2E6930 : 0xFF1E4620);
            renderer.drawBorder(x + 10, curY, grpBtnW, 20, 0xFF55FF55, 1);
            renderer.drawCenteredTextWithShadow("+ Grupuj zaznaczenie (Ctrl+G)", x + 10 + grpBtnW / 2, curY + 6, 0xFFFFFFFF);
            curY += 26;
        }

        Collection<HudGroup> groups = layout.getGroups().values();
        if (groups.isEmpty()) {
            renderer.drawText("Brak utworzonych grup.", x + 12, curY + 10, 0xFF888888, false, 0.8f, true);
            renderer.drawText("Zaznacz min. 2 elementy i", x + 12, curY + 26, 0xFF666666, false, 0.75f, false);
            renderer.drawText("użyj Ctrl+G, aby utworzyć grupę.", x + 12, curY + 38, 0xFF666666, false, 0.75f, false);
            return;
        }

        int itemH = 36;
        int itemGap = 4;
        int listH = height - (curY - y) - 8;
        int totalH = groups.size() * (itemH + itemGap);
        this.maxScroll = Math.max(0, totalH - listH);

        int drawY = curY - state.getListScrollOffset();
        for (HudGroup group : groups) {
            if (drawY + itemH >= curY && drawY <= curY + listH) {
                renderGroupItem(renderer, group, x + 8, drawY, width - 16, itemH, mouseX, mouseY);
            }
            drawY += itemH + itemGap;
        }
    }

    private void renderGroupItem(IHudRenderer renderer, HudGroup group,
                                int itemX, int itemY, int itemW, int itemH, int mouseX, int mouseY) {
        boolean hover = mouseX >= itemX && mouseX <= itemX + itemW && mouseY >= itemY && mouseY <= itemY + itemH;
        renderer.fill(itemX, itemY, itemW, itemH, hover ? 0xFF222632 : 0xFF1C1E27);
        renderer.drawBorder(itemX, itemY, itemW, itemH, 0xFF2A2E3D, 1);

        // Color indicator pill
        renderer.fill(itemX + 6, itemY + 8, 4, itemH - 16, group.getColor());

        // Group Title & Members count
        renderer.drawText(group.getName(), itemX + 16, itemY + 6, 0xFFE0E0E0, true, 0.85f, true);
        String sub = String.format("%d elementów", group.getComponentIds().size());
        renderer.drawText(sub, itemX + 16, itemY + 20, 0xFF888888, false, 0.7f, false);

        // Delete / Ungroup Button (✕)
        int delX = itemX + itemW - 22;
        int delY = itemY + 9;
        boolean delHover = mouseX >= delX && mouseX <= delX + 16 && mouseY >= delY && mouseY <= delY + 16;
        renderer.fill(delX, delY, 16, 16, delHover ? 0xFF442222 : 0xFF16181F);
        renderer.drawBorder(delX, delY, 16, 16, 0xFFFF5555, 1);
        renderer.drawCenteredTextWithShadow("✕", delX + 8, delY + 4, 0xFFFF7777);
    }

    private void renderCollapsedStrip(IHudRenderer renderer, int mouseX, int mouseY) {
        renderer.fill(x, y, 24, height, 0xEE16181F);
        renderer.fill(x + 23, y, 1, height, 0xFF2A2E3D);

        boolean hover = mouseX >= x && mouseX <= x + 24 && mouseY >= y + 8 && mouseY <= y + 24;
        renderer.fill(x + 4, y + 8, 16, 16, hover ? 0xFF353A4C : 0xFF232734);
        renderer.drawText("▶", x + 9, y + 12, 0xFFCCCCCC, false, 0.8f, false);
    }

    private void renderComponentItem(IHudRenderer renderer, HudLayout layout, HudComponentDescriptor desc,
                                     int itemX, int itemY, int itemW, int itemH,
                                     boolean isSelected, int mouseX, int mouseY) {
        HudComponentState compState = layout.getComponentState(desc.getId());
        boolean isVisible = compState != null && compState.isVisible();

        boolean isHovered = mouseX >= itemX && mouseX <= itemX + itemW && mouseY >= itemY && mouseY <= itemY + itemH;
        int bg = isSelected ? 0xFF2D384E : (isHovered ? 0xFF222632 : 0xFF1C1E27);
        renderer.fill(itemX, itemY, itemW, itemH, bg);

        int border = isSelected ? 0xFFFFCC00 : (isHovered ? 0xFF4A90E2 : 0xFF2A2E3D);
        renderer.drawBorder(itemX, itemY, itemW, itemH, border, 1);

        // Visibility Toggle Button (Eye / Dash)
        int toggleX = itemX + 6;
        int toggleY = itemY + 8;
        int toggleSize = 18;
        boolean toggleHover = mouseX >= toggleX && mouseX <= toggleX + toggleSize && mouseY >= toggleY && mouseY <= toggleY + toggleSize;
        renderer.fill(toggleX, toggleY, toggleSize, toggleSize, toggleHover ? 0xFF353A4C : 0xFF16181F);
        renderer.drawBorder(toggleX, toggleY, toggleSize, toggleSize, isVisible ? 0xFF55FF55 : 0xFF555555, 1);
        String toggleIcon = isVisible ? "👁" : "—";
        renderer.drawCenteredTextWithShadow(toggleIcon, toggleX + toggleSize / 2, toggleY + 4, isVisible ? 0xFF55FF55 : 0xFF888888);

        // Name and Type Badge
        int textX = toggleX + toggleSize + 8;
        renderer.drawText(desc.getDisplayName(), textX, itemY + 6, isSelected ? 0xFFFFCC00 : 0xFFE0E0E0, true, 0.85f, true);

        String typeBadge = "[" + desc.getCategory() + " • " + desc.getType().name() + "]";
        renderer.drawText(typeBadge, textX, itemY + 18, 0xFF888888, false, 0.7f, false);
    }

    private void updateFilteredList(HudEditorState state) {
        filteredDescriptors.clear();
        HudComponentRegistry registry = HudComponentRegistry.getInstance();
        String query = state.getSearchQuery().trim().toLowerCase(Locale.ROOT);
        String cat = state.getSelectedCategory();

        for (HudComponentDescriptor desc : registry.getAllDescriptors()) {
            boolean matchesCat = cat.equalsIgnoreCase("ALL") || desc.getCategory().equalsIgnoreCase(cat);
            boolean matchesQuery = query.isEmpty() ||
                    desc.getId().toLowerCase(Locale.ROOT).contains(query) ||
                    desc.getDisplayName().toLowerCase(Locale.ROOT).contains(query) ||
                    desc.getCategory().toLowerCase(Locale.ROOT).contains(query);

            if (matchesCat && matchesQuery) {
                filteredDescriptors.add(desc);
            }
        }
        filteredDescriptors.sort(Comparator.comparing(HudComponentDescriptor::getDisplayName));
    }

    public boolean handleClick(int mouseX, int mouseY, int button, HudLayout layout,
                               HudEditorState state, HudSelectionManager selectionManager) {
        if (state.isLeftPanelCollapsed()) {
            if (mouseX >= x && mouseX <= x + 24 && mouseY >= y + 8 && mouseY <= y + 24) {
                state.setLeftPanelCollapsed(false);
                return true;
            }
            return false;
        }

        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
            return false;
        }

        // Collapse button click
        int collapseBtnX = x + width - 24;
        int collapseBtnY = y + 8;
        if (mouseX >= collapseBtnX && mouseX <= collapseBtnX + 16 && mouseY >= collapseBtnY && mouseY <= collapseBtnY + 16) {
            state.setLeftPanelCollapsed(true);
            return true;
        }

        // Tab Strip Clicks
        int tabY = y + 28;
        int tabH = 18;
        int tabW = (width - 24) / 3;
        if (mouseY >= tabY && mouseY <= tabY + tabH) {
            if (mouseX >= x + 10 && mouseX <= x + 10 + tabW) {
                state.setLeftPanelTab(HudEditorState.LeftPanelTab.COMPONENTS);
                state.setListScrollOffset(0);
                return true;
            }
            if (mouseX >= x + 12 + tabW && mouseX <= x + 12 + 2 * tabW) {
                state.setLeftPanelTab(HudEditorState.LeftPanelTab.LAYERS);
                state.setListScrollOffset(0);
                return true;
            }
            if (mouseX >= x + 14 + 2 * tabW && mouseX <= x + 14 + 3 * tabW) {
                state.setLeftPanelTab(HudEditorState.LeftPanelTab.GROUPS);
                state.setListScrollOffset(0);
                return true;
            }
        }

        switch (state.getLeftPanelTab()) {
            case LAYERS:
                return handleLayersClick(mouseX, mouseY, layout, state);
            case GROUPS:
                return handleGroupsClick(mouseX, mouseY, layout, state);
            case COMPONENTS:
            default:
                return handleComponentsClick(mouseX, mouseY, layout, state, selectionManager);
        }
    }

    private boolean handleComponentsClick(int mouseX, int mouseY, HudLayout layout,
                                          HudEditorState state, HudSelectionManager selectionManager) {
        // Search input box click
        int searchX = x + 10;
        int searchY = y + 50;
        int searchW = width - 20;
        int searchH = 20;
        if (mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + searchH) {
            state.setSearching(true);
            return true;
        } else {
            state.setSearching(false);
        }

        // Category filter click
        int catY = searchY + 24;
        String[] categories = new String[]{"ALL", "HUD", "PvP", "Survival", "Visual", "Other"};
        int catBtnW = (width - 24) / 3;
        int catBtnH = 16;
        for (int i = 0; i < categories.length; i++) {
            int cx = x + 10 + (i % 3) * (catBtnW + 2);
            int cy = catY + (i / 3) * (catBtnH + 3);
            if (mouseX >= cx && mouseX <= cx + catBtnW && mouseY >= cy && mouseY <= cy + catBtnH) {
                state.setSelectedCategory(categories[i]);
                state.setListScrollOffset(0);
                return true;
            }
        }

        // Component item list clicks
        int listY = catY + 40;
        int itemH = 34;
        int itemGap = 4;
        int currentY = listY - state.getListScrollOffset();

        for (HudComponentDescriptor desc : filteredDescriptors) {
            if (mouseY >= currentY && mouseY <= currentY + itemH) {
                int toggleX = x + 8 + 6;
                int toggleY = currentY + 8;
                int toggleSize = 18;

                // Toggle visibility click
                if (mouseX >= toggleX && mouseX <= toggleX + toggleSize && mouseY >= toggleY && mouseY <= toggleY + toggleSize) {
                    HudComponentState compState = layout.getComponentState(desc.getId());
                    if (compState != null) {
                        boolean newVis = !compState.isVisible();
                        state.getUndoRedoManager().executeCommand(new ChangeVisibilityCommand(desc.getId(), compState.isVisible(), newVis, layout));
                    }
                    return true;
                }

                // Row click -> Select component
                selectionManager.select(desc.getId());
                return true;
            }
            currentY += itemH + itemGap;
        }

        return true;
    }

    private boolean handleLayersClick(int mouseX, int mouseY, HudLayout layout, HudEditorState state) {
        int curY = y + 52 + 16;
        List<HudLayer> layers = new ArrayList<>(layout.getLayers().values());
        layers.sort((a, b) -> Integer.compare(b.getZIndexBase(), a.getZIndexBase()));

        int itemH = 36;
        int itemGap = 4;
        int drawY = curY - state.getListScrollOffset();

        for (HudLayer layer : layers) {
            if (mouseY >= drawY && mouseY <= drawY + itemH) {
                int itemW = width - 16;
                int toggleY = drawY + 9;
                int eyeX = x + 8 + itemW - 46;
                if (mouseX >= eyeX && mouseX <= eyeX + 18 && mouseY >= toggleY && mouseY <= toggleY + 18) {
                    layer.setVisible(!layer.isVisible());
                    return true;
                }
                int lockX = x + 8 + itemW - 24;
                if (mouseX >= lockX && mouseX <= lockX + 18 && mouseY >= toggleY && mouseY <= toggleY + 18) {
                    layer.setLocked(!layer.isLocked());
                    return true;
                }
                return true;
            }
            drawY += itemH + itemGap;
        }

        // Add Layer button
        int addBtnY = y + height - 28;
        int addBtnW = width - 20;
        if (mouseX >= x + 10 && mouseX <= x + 10 + addBtnW && mouseY >= addBtnY && mouseY <= addBtnY + 20) {
            String newId = "layer_" + (layout.getLayers().size() + 1);
            state.getLayerManager().createLayer(layout, newId, "Warstwa " + (layout.getLayers().size() + 1), 500);
            return true;
        }

        return true;
    }

    private boolean handleGroupsClick(int mouseX, int mouseY, HudLayout layout, HudEditorState state) {
        int curY = y + 52 + 16;
        HudMultiSelectionManager multiSel = state.getMultiSelectionManager();

        if (multiSel.getSelectedCount() > 1) {
            int grpBtnW = width - 20;
            if (mouseX >= x + 10 && mouseX <= x + 10 + grpBtnW && mouseY >= curY && mouseY <= curY + 20) {
                String grpId = "group_" + (layout.getGroups().size() + 1);
                String grpName = "Grupa " + (layout.getGroups().size() + 1);
                HudGroup grp = state.getGroupManager().createGroup(layout, grpId, grpName, multiSel.getSelectedIds());
                if (grp != null) {
                    state.getUndoRedoManager().recordExecutedCommand(new CreateGroupCommand(grp, layout));
                }
                return true;
            }
            curY += 26;
        }

        Collection<HudGroup> groups = layout.getGroups().values();
        int itemH = 36;
        int itemGap = 4;
        int drawY = curY - state.getListScrollOffset();

        for (HudGroup group : groups) {
            if (mouseY >= drawY && mouseY <= drawY + itemH) {
                int itemW = width - 16;
                int delX = x + 8 + itemW - 22;
                int delY = drawY + 9;

                // Delete / Ungroup button
                if (mouseX >= delX && mouseX <= delX + 16 && mouseY >= delY && mouseY <= delY + 16) {
                    state.getUndoRedoManager().executeCommand(new DeleteGroupCommand(group, layout));
                    return true;
                }

                // Click on group -> Select all components in group
                multiSel.selectAll(group.getComponentIds());
                return true;
            }
            drawY += itemH + itemGap;
        }

        return true;
    }

    public boolean handleScroll(double amount, HudEditorState state) {
        if (state.isLeftPanelCollapsed()) return false;
        int newOffset = (int) (state.getListScrollOffset() - amount * 20);
        state.setListScrollOffset(Math.max(0, Math.min(newOffset, maxScroll)));
        return true;
    }

    public int getWidth(HudEditorState state) {
        return state.isLeftPanelCollapsed() ? 24 : width;
    }
}
