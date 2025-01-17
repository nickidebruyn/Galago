package com.galago.editor.ui.panels;

import com.bruynhuis.galago.ui.Image;
import com.bruynhuis.galago.ui.Label;
import com.bruynhuis.galago.ui.TextAlign;
import com.bruynhuis.galago.ui.button.TouchButton;
import com.bruynhuis.galago.ui.effect.TouchEffect;
import com.bruynhuis.galago.ui.listener.KeyboardListener;
import com.bruynhuis.galago.ui.listener.TouchButtonAdapter;
import com.bruynhuis.galago.ui.listener.TouchButtonListener;
import com.bruynhuis.galago.ui.listener.ValueChangeListener;
import com.bruynhuis.galago.ui.panel.Panel;
import com.bruynhuis.galago.ui.panel.VFlowPanel;
import com.galago.editor.ui.Button;
import com.galago.editor.ui.ButtonGroup;
import com.galago.editor.ui.LongField;
import com.galago.editor.ui.SliderField;
import com.galago.editor.ui.SpinnerButton;
import com.galago.editor.ui.actions.TerrainAction;
import com.galago.editor.utils.Action;
import com.galago.editor.utils.EditorUtils;
import com.bruynhuis.galago.util.MaterialUtils;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ndebruyn
 */
public class TerrainPanel extends Panel {

    private TerrainAction terrainAction = new TerrainAction();
    private TouchButton header;
    private TouchButton headerMaterial;
    private VFlowPanel flowPanel;
    private TouchButton hoverButton;
    private Button generateButton;
    private SpinnerButton terrainTypeButton;
    private SpinnerButton terrainSizeButton;
    private SpinnerButton terrainMaterialButton;
    private SpinnerButton terrainLayersButton;
    private SpinnerButton terrainToolsButton;
    private String[] terrainTypes = {"Flat", "Island", "Midpoint", "Image"};
    private String[] terrainSize = {"256", "512", "1024"};
    private String[] terrainMaterialTypes = {"Paintable", "Height Based", "PBR"};
    private String[] terrainTextureLayers = {"Base", "Layer 1", "Layer 2", "Layer 3"};
    private String[] terrainHeightBasedTextureLayers = {"Base", "Layer 1", "Layer 2", "Layer 3", "Slope"};
    private String[] terrainToolTypesPaint = {"Paint", "Raise", "Flatten", "Smooth", "Grass 1x1", "Grass 1x2", "Grass 2x1", "Trees 1"};
    private String[] terrainToolTypesHeight = {"Paint", "Raise", "Flatten", "Smooth"};

    private SliderField iterationsField;
    private SliderField minField;
    private SliderField maxField;
    private SliderField rangeField;
    private SliderField persistenceField;
    private LongField seedField;
    private TouchButton heightMapButton;
    private ButtonGroup baseTextureButton;
    private TouchButton baseModelButton;
    private SliderField baseTextureScale;
    private SliderField textureRoughness;
    private SliderField textureMetallic;
    private SliderField slopeScale;
    private SliderField startHeight;
    private SliderField endHeight;

    private SliderField paintRadius;
    private SliderField paintStrength;
    private SliderField paintScale;
    private Button autoPainterButton;
    private SliderField autoPaintMin;
    private SliderField autoPaintMax;
    private SliderField autoPaintStartHeight;

    private String selectedDiffuseMap = "DiffuseMap";
    private String selectedNormalMap = "NormalMap";
    private String selectedTextureScale = "DiffuseMap_0_scale";
    private String selectedRegion = "region1";
    private String selectedRoughness = "Roughness_0";
    private String selectedMetallic = "Metallic_0";

    private BatchNode selectedBatchLayer;
    private BatchNode selectedInstancedNode;

    private float triScale = 100;

    private boolean slopeSelected = false;

    private TerrainQuad terrain;

    public TerrainPanel(Panel parent) {
        super(parent, "Interface/panel-left.png", EditorUtils.HIERARCHYBAR_WIDTH, parent.getWindow().getHeight());

        setBackgroundColor(EditorUtils.theme.getPanelColor());

        flowPanel = new VFlowPanel(this, null, EditorUtils.HIERARCHYBAR_WIDTH, parent.getWindow().getHeight());
        this.add(flowPanel);
        flowPanel.centerTop(0, 0);

        //Panel HEADER
        header = new TouchButton(flowPanel, "terrain-header", "Interface/hierarchy-header.png", EditorUtils.HIERARCHYBAR_WIDTH - 4, 32);
        header.setText("Terrain");
        header.setTextColor(EditorUtils.theme.getHeaderTextColor());
        header.setBackgroundColor(EditorUtils.theme.getHeaderColor());
        header.setTextAlignment(TextAlign.LEFT);

        //TERRAIN TYPE
        terrainTypeButton = createLabeledSpinner("Type", "terrain-type", terrainTypes);

        terrainTypeButton.addTouchButtonListener(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                System.out.println("Selected terrain type: " + terrainTypeButton.getIndex());

                if (terrainTypeButton.getIndex() == 0) {
                    terrainAction.setType(TerrainAction.TYPE_FLAT);

                } else if (terrainTypeButton.getIndex() == 1) {
                    terrainAction.setType(TerrainAction.TYPE_ISLAND);

                } else if (terrainTypeButton.getIndex() == 2) {
                    terrainAction.setType(TerrainAction.TYPE_MIDPOINT);

                } else if (terrainTypeButton.getIndex() == 3) {
                    terrainAction.setType(TerrainAction.TYPE_IMAGE);

                }

                reload();
            }
        });

        terrainSizeButton = createLabeledSpinner("Size", "terrain-size", terrainSize);
        terrainSizeButton.addTouchButtonListener(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                System.out.println("Selected terrain size: " + terrainSizeButton.getIndex());

                if (terrainSizeButton.getIndex() == 0) {
                    terrainAction.setTerrainSize(256);

                } else if (terrainSizeButton.getIndex() == 1) {
                    terrainAction.setTerrainSize(512);

                } else if (terrainSizeButton.getIndex() == 2) {
                    terrainAction.setTerrainSize(1024);

                }

            }
        });

        terrainMaterialButton = createLabeledSpinner("Material", "terrain-material", terrainMaterialTypes);
        terrainMaterialButton.addTouchButtonListener(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                System.out.println("Selected terrain material: " + terrainMaterialButton.getIndex());

                terrainAction.setTerrainMaterial(terrainMaterialButton.getIndex());

            }
        });

        iterationsField = createLabeledSlider("Iterations", 100, 10000, 10);
        iterationsField.setDecimals(false);
        iterationsField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                terrainAction.setIterations((int) value);
            }

        });

        minField = createLabeledSlider("Min Radius", 1, 100, 1);
        minField.setDecimals(false);
        minField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                terrainAction.setMinRadius(value);
            }

        });

        maxField = createLabeledSlider("Max Radius", 1, 100, 1);
        maxField.setDecimals(false);
        maxField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                terrainAction.setMaxRadius(value);
            }

        });

        rangeField = createLabeledSliderDecimal("Range", -1, 1, 0.1f);
        rangeField.setDecimals(true);
        rangeField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                terrainAction.setRange(value);

            }

        });

        persistenceField = createLabeledSliderDecimal("Range", 0, 1, 0.1f);
        persistenceField.setDecimals(true);
        persistenceField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                terrainAction.setPersistence(value);

            }

        });

        seedField = createLabeledLongInput("Seed", "seed-terrain");
        seedField.addKeyboardListener(new KeyboardListener() {

            @Override
            public void doKeyPressed(KeyInputEvent evt) {
                //System.out.println("Pressed: " + evt.getKeyChar());

                try {
                    terrainAction.setSeed(seedField.getValue());

                } catch (Exception e) {
                    seedField.setValue(terrainAction.getSeed());
                    e.printStackTrace();

                }

            }

        });

        heightMapButton = createLabeledTextureButton("Height Map", "terrain-heightmap-button");
        heightMapButton.addTouchButtonListener(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {

            }

        });

        generateButton = new Button(flowPanel, "generate-terrain-button", "Generate");
        generateButton.addTouchButtonListener(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                //terrainAction.setSeed(FastMath.nextRandomInt(0, 10000));
                window.getApplication().getMessageManager().sendMessage(Action.CREATE_TERRAIN, terrainAction);
            }

        });

        //Panel Materials
        headerMaterial = new TouchButton(flowPanel, "terrain-material-header", "Interface/hierarchy-header.png", EditorUtils.HIERARCHYBAR_WIDTH - 4, 32);
        headerMaterial.setText("Materials");
        headerMaterial.setTextColor(EditorUtils.theme.getHeaderTextColor());
        headerMaterial.setBackgroundColor(EditorUtils.theme.getHeaderColor());
        headerMaterial.setTextAlignment(TextAlign.LEFT);

        terrainToolsButton = createLabeledSpinner("Tool", "terrain-tool", terrainToolTypesPaint);
        terrainToolsButton.addTouchButtonListener(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                System.out.println("Selected terrain size: " + terrainToolsButton.getIndex());
                terrainAction.setTool(terrainToolsButton.getIndex());
                reload();

            }
        });

        terrainLayersButton = createLabeledSpinner("Layers", "terrain-textures", terrainTextureLayers);
        terrainLayersButton.addTouchButtonListener(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {

                int index = terrainLayersButton.getIndex();
                System.out.println("Selected terrain layer: " + index);

                if (isPBRMaterial()) {
                    selectedDiffuseMap = "AlbedoMap_" + index;
                    selectedNormalMap = "NormalMap_" + index;
                    selectedTextureScale = "AlbedoMap_" + index + "_scale";
                    selectedRoughness = "Roughness_" + index;
                    selectedMetallic = "Metallic_" + index;

                } else {
                    if (index == 0) {
                        selectedDiffuseMap = "DiffuseMap";
                        selectedNormalMap = "NormalMap";
                        selectedTextureScale = "DiffuseMap_0_scale";

                    } else {
                        selectedDiffuseMap = "DiffuseMap_" + index;
                        selectedNormalMap = "NormalMap_" + index;
                        selectedTextureScale = "DiffuseMap_" + index + "_scale";

                    }
                }

                if (index == 4) {
                    selectedDiffuseMap = "SlopeDiffuseMap";

                }

                baseTextureButton.getButton1().setId(selectedDiffuseMap);
                baseTextureButton.getButton2().setId(selectedNormalMap);

                if (isHeightLitMaterial() && index < 4) {
                    selectedRegion = "region" + (index + 1);
                    slopeSelected = false;

                } else {
                    selectedRegion = null;
                    slopeSelected = true;
                }

                reload();

            }
        });

        baseTextureButton = createLabeledTextureButtonGroup("Textures", "DiffuseMap", "NormalMap");

        baseModelButton = createLabeledModelButton("Model", "terrain-model");

        baseTextureScale = createLabeledSlider("Scale", 1, 256, 1);
        baseTextureScale.setDecimals(false);
        baseTextureScale.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                if (isHeightLitMaterial()) {
                    Vector3f region = terrain.getMaterial().getParamValue(selectedRegion);
                    region.setZ(value);

                } else {
                    terrain.getMaterial().setFloat(selectedTextureScale, value / triScale);
                }

            }

        });

        textureRoughness = createLabeledSliderDecimal("Roughness", 0, 1, 0.1f);
        textureRoughness.setDecimals(true);
        textureRoughness.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                terrain.getMaterial().setFloat(selectedRoughness, value);
            }

        });

        textureMetallic = createLabeledSliderDecimal("Metallic", 0, 1, 0.1f);
        textureMetallic.setDecimals(true);
        textureMetallic.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                terrain.getMaterial().setFloat(selectedMetallic, value);
            }

        });

        slopeScale = createLabeledSlider("Scale", 1, 256, 1);
        slopeScale.setDecimals(false);
        slopeScale.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                if (isHeightLitMaterial() && slopeSelected) {
                    terrain.getMaterial().setFloat("slopeTileFactor", value);

                }

            }

        });

        startHeight = createLabeledSlider("Start Height", -64, 128, 1);
        startHeight.setDecimals(false);
        startHeight.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                if (isHeightLitMaterial()) {
                    Vector3f region = terrain.getMaterial().getParamValue(selectedRegion);
                    region.setX(value);

                }

            }

        });

        endHeight = createLabeledSlider("End Height", -64, 128, 1);
        endHeight.setDecimals(false);
        endHeight.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                if (isHeightLitMaterial()) {
                    Vector3f region = terrain.getMaterial().getParamValue(selectedRegion);
                    region.setY(value);

                }

            }

        });

        paintRadius = createLabeledSlider("Radius", 1, 50, 1);
        paintRadius.setDecimals(true);
        paintRadius.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {

            }

        });
        paintRadius.setValue(5);

        paintStrength = createLabeledSliderDecimal("Strength", 0.1f, 1f, 0.1f);
        paintStrength.setDecimals(true);
        paintStrength.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {

            }

        });
        paintStrength.setValue(1);

        paintScale = createLabeledSliderDecimal("Scale", 0.5f, 2f, 1f);
        paintScale.setDecimals(true);
        paintScale.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                terrainAction.setScaleFactor(value);

            }

        });
        paintScale.setValue(1);

        autoPaintMin = createLabeledSlider("Auto min", 0, 1, 1);
        autoPaintMin.setDecimals(true);
        autoPaintMin.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                terrainAction.setAutoPaintMin(value);

            }

        });
        autoPaintMin.setValue(0);

        autoPaintMax = createLabeledSlider("Auto max", 0, 1, 1);
        autoPaintMax.setDecimals(true);
        autoPaintMax.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                terrainAction.setAutoPaintMax(value);

            }

        });
        autoPaintMax.setValue(0.6f);

        autoPaintStartHeight = createLabeledSlider("Auto height", 0, 100, 1);
        autoPaintStartHeight.setDecimals(true);
        autoPaintStartHeight.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void doValueChange(float value) {
                terrainAction.setAutoStartHeight(value);

            }

        });
        autoPaintStartHeight.setValue(0);

        autoPainterButton = new Button(flowPanel, "auto-paint-button", "Auto Paint");
        autoPainterButton.addTouchButtonListener(new TouchButtonAdapter() {
            @Override
            public void doTouchUp(float touchX, float touchY, float tpf, String uid) {
                window.getApplication().getMessageManager().sendMessage(Action.AUTO_PAINT, terrainAction);
            }

        });

        flowPanel.layout();

//        hoverButton = new TouchButton(this, "terrain-hover", "Interface/blank.png", EditorUtils.HIERARCHYBAR_WIDTH, parent.getWindow().getHeight());
//        hoverButton.centerTop(0, 0);
//        hoverButton.setTransparency(0.1f);
//        hoverButton.addTouchButtonListener(new TouchButtonAdapter() {
//            @Override
//            public void doHoverOff(float touchX, float touchY, float tpf, String uid) {
//                window.getApplication().getMessageManager().sendMessage(Action.UI_OFF, null);
//            }
//
//            @Override
//            public void doHoverOver(float touchX, float touchY, float tpf, String uid) {
//                window.getApplication().getMessageManager().sendMessage(Action.UI_OVER, null);
//            }
//
//        });
        parent.add(this);

    }

    private SpinnerButton createLabeledSpinner(String text, String id, String[] options) {
        Panel panel = new Panel(flowPanel, null, EditorUtils.HIERARCHYBAR_WIDTH - 6, 32);
        flowPanel.add(panel);

        Label label = new Label(panel, text, 14, EditorUtils.HIERARCHYBAR_WIDTH * 0.5f, 32);
        label.setAlignment(TextAlign.LEFT);
        label.setVerticalAlignment(TextAlign.CENTER);
        label.leftCenter(5, 0);

        SpinnerButton spinner = new SpinnerButton(panel, id, options);
        spinner.rightCenter(5, 0);

        return spinner;
    }

    private SliderField createLabeledSlider(String text, int min, int max, int increment) {
        Panel panel = new Panel(flowPanel, null, EditorUtils.HIERARCHYBAR_WIDTH - 6, 32);
        flowPanel.add(panel);

        Label label = new Label(panel, text, 14, EditorUtils.HIERARCHYBAR_WIDTH * 0.5f, 32);
        label.setAlignment(TextAlign.LEFT);
        label.setVerticalAlignment(TextAlign.CENTER);
        label.leftCenter(5, 0);

        SliderField field = new SliderField(panel, min, max, increment);
        field.rightCenter(5, 0);

        return field;
    }

    private SliderField createLabeledSliderDecimal(String text, float min, float max, float increment) {
        Panel panel = new Panel(flowPanel, null, EditorUtils.HIERARCHYBAR_WIDTH - 6, 32);
        flowPanel.add(panel);

        Label label = new Label(panel, text, 14, EditorUtils.HIERARCHYBAR_WIDTH * 0.5f, 32);
        label.setAlignment(TextAlign.LEFT);
        label.setVerticalAlignment(TextAlign.CENTER);
        label.leftCenter(5, 0);

        SliderField field = new SliderField(panel, min, max, increment);
        field.rightCenter(5, 0);

        return field;
    }

    private LongField createLabeledLongInput(String text, String id) {
        Panel panel = new Panel(flowPanel, null, EditorUtils.HIERARCHYBAR_WIDTH - 6, 32);
        flowPanel.add(panel);

        Label label = new Label(panel, text, 14, EditorUtils.HIERARCHYBAR_WIDTH * 0.5f, 32);
        label.setAlignment(TextAlign.LEFT);
        label.setVerticalAlignment(TextAlign.CENTER);
        label.leftCenter(5, 0);

        LongField field = new LongField(panel, id);
        field.rightCenter(5, 0);

        return field;
    }

    private TouchButton createLabeledTextureButton(String text, String id) {
        Panel panel = new Panel(flowPanel, null, EditorUtils.HIERARCHYBAR_WIDTH - 6, 80);
        flowPanel.add(panel);

        Label label = new Label(panel, text, 14, EditorUtils.HIERARCHYBAR_WIDTH * 0.5f, 60);
        label.setAlignment(TextAlign.LEFT);
        label.setVerticalAlignment(TextAlign.CENTER);
        label.leftTop(5, 5);

        Image img = new Image(panel, "Interface/texture-button.png", 70, 70);
        img.setBackgroundColor(EditorUtils.theme.getButtonColor());
        img.rightTop(0, 10);

        TouchButton button = new TouchButton(panel, id, "Interface/texture-button.png", 60, 60);
        button.rightTop(5, 15);
        button.addEffect(new TouchEffect(button));
        button.getPicture().setMaterial(button.getPicture().getMaterial().clone());

        return button;
    }

    private ButtonGroup createLabeledTextureButtonGroup(String text, String idB1, String idB2) {
        Panel panel = new Panel(flowPanel, null, EditorUtils.HIERARCHYBAR_WIDTH - 6, 80);
        flowPanel.add(panel);

        Label label = new Label(panel, text, 12, EditorUtils.HIERARCHYBAR_WIDTH * 0.5f, 60);
        label.setAlignment(TextAlign.LEFT);
        label.setVerticalAlignment(TextAlign.CENTER);
        label.leftTop(5, 5);

        Image img = new Image(panel, "Interface/texture-button.png", 70, 70);
        img.setBackgroundColor(EditorUtils.theme.getButtonColor());
        img.rightTop(5, 10);

        Image img2 = new Image(panel, "Interface/texture-button.png", 70, 70);
        img2.setBackgroundColor(EditorUtils.theme.getButtonColor());
        img2.rightTop(85, 10);

        TouchButton button1 = new TouchButton(panel, idB1, "Interface/texture-button.png", 60, 60);
        button1.rightTop(90, 15);
        button1.addEffect(new TouchEffect(button1));
        button1.getPicture().setMaterial(button1.getPicture().getMaterial().clone());

        TouchButton button2 = new TouchButton(panel, idB2, "Interface/texture-button.png", 60, 60);
        button2.rightTop(10, 15);
        button2.addEffect(new TouchEffect(button2));
        button2.getPicture().setMaterial(button2.getPicture().getMaterial().clone());
        
        TouchButton leftButton = new TouchButton(panel, "left-remove-button-"+idB1);
        leftButton.leftTop(15, 20);
        leftButton.addEffect(new TouchEffect(leftButton));
        
        TouchButton rightButton = new TouchButton(panel, "right-remove-button-"+idB1);
        rightButton.rightTop(15, 20);
        rightButton.addEffect(new TouchEffect(rightButton));

        return new ButtonGroup(button1, button2, img2, img, leftButton, rightButton);
    }

    private TouchButton createLabeledModelButton(String text, String id) {
        Panel panel = new Panel(flowPanel, null, EditorUtils.HIERARCHYBAR_WIDTH - 6, 80);
        flowPanel.add(panel);

        Label label = new Label(panel, text, 14, EditorUtils.HIERARCHYBAR_WIDTH * 0.5f, 60);
        label.setAlignment(TextAlign.LEFT);
        label.setVerticalAlignment(TextAlign.CENTER);
        label.leftTop(5, 5);

        Image img = new Image(panel, "Interface/texture-button.png", 70, 70);
        img.setBackgroundColor(EditorUtils.theme.getButtonColor());
        img.rightTop(0, 10);

        TouchButton button = new TouchButton(panel, id, "Interface/texture-button.png", 60, 60);
        button.rightTop(5, 15);
        button.addEffect(new TouchEffect(button));
        button.getPicture().setMaterial(button.getPicture().getMaterial().clone());

        return button;
    }

    @Override
    public void show() {
        super.show();
        reload();
    }

    public void reload() {
//        this.terrainAction = new TerrainAction();
//        this.terrainAction.setType(TerrainAction.TYPE_FLAT);

        if (terrainAction.getType() == TerrainAction.TYPE_FLAT) {
            terrainSizeButton.getParent().setVisible(true);
            iterationsField.getParent().setVisible(false);
            minField.getParent().setVisible(false);
            maxField.getParent().setVisible(false);
            seedField.getParent().setVisible(false);
            rangeField.getParent().setVisible(false);
            persistenceField.getParent().setVisible(false);
            heightMapButton.getParent().setVisible(false);

        } else if (terrainAction.getType() == TerrainAction.TYPE_ISLAND) {
            terrainSizeButton.getParent().setVisible(true);
            iterationsField.getParent().setVisible(true);
            minField.getParent().setVisible(true);
            maxField.getParent().setVisible(true);
            seedField.getParent().setVisible(true);
            rangeField.getParent().setVisible(false);
            persistenceField.getParent().setVisible(false);
            heightMapButton.getParent().setVisible(false);

            iterationsField.setValue(terrainAction.getIterations());
            minField.setValue(terrainAction.getMinRadius());
            maxField.setValue(terrainAction.getMaxRadius());
            seedField.setValue(terrainAction.getSeed());

        } else if (terrainAction.getType() == TerrainAction.TYPE_MIDPOINT) {
            terrainSizeButton.getParent().setVisible(true);
            iterationsField.getParent().setVisible(false);
            minField.getParent().setVisible(false);
            maxField.getParent().setVisible(false);
            seedField.getParent().setVisible(true);
            rangeField.getParent().setVisible(true);
            persistenceField.getParent().setVisible(true);
            heightMapButton.getParent().setVisible(false);

            rangeField.setValue(terrainAction.getRange());
            persistenceField.setValue(terrainAction.getPersistence());
            seedField.setValue(terrainAction.getSeed());

        } else if (terrainAction.getType() == TerrainAction.TYPE_IMAGE) {
            terrainSizeButton.getParent().setVisible(false);
            iterationsField.getParent().setVisible(false);
            minField.getParent().setVisible(false);
            maxField.getParent().setVisible(false);
            seedField.getParent().setVisible(false);
            rangeField.getParent().setVisible(false);
            persistenceField.getParent().setVisible(false);
            heightMapButton.getParent().setVisible(true);

        }

        if (terrain == null) {

            headerMaterial.setVisible(false);
            terrainToolsButton.getParent().setVisible(false);
            terrainLayersButton.getParent().setVisible(false);
            baseTextureButton.getButton1().getParent().setVisible(false);
            baseModelButton.getParent().setVisible(false);
            baseTextureScale.getParent().setVisible(false);
            textureRoughness.getParent().setVisible(false);
            textureMetallic.getParent().setVisible(false);
            startHeight.getParent().setVisible(false);
            endHeight.getParent().setVisible(false);
            slopeScale.getParent().setVisible(false);
            paintRadius.getParent().setVisible(false);
            paintStrength.getParent().setVisible(false);
            paintScale.getParent().setVisible(false);
            autoPainterButton.setVisible(false);
            autoPaintMin.getParent().setVisible(false);
            autoPaintMax.getParent().setVisible(false);
            autoPaintStartHeight.getParent().setVisible(false);

        } else {

            //Set this when a saved level was open
            if (isHeightLitMaterial()) {
                terrainMaterialButton.setSelection(1);

            } else if (isPaintableMaterial()) {
                terrainMaterialButton.setSelection(0);

            }
            int size = terrain.getTerrainSize() - 1;
            System.out.println("Terrain size is: " + size);
            if (size == 256) {
                terrainSizeButton.setSelection(0);
            }
            if (size == 512) {
                terrainSizeButton.setSelection(1);
            }
            if (size == 1024) {
                terrainSizeButton.setSelection(2);
            }

            //Set the terrain settings
            headerMaterial.setVisible(true);
            terrainToolsButton.getParent().setVisible(true);
            terrainLayersButton.getParent().setVisible(true);
            baseTextureButton.getButton1().getParent().setVisible(true);

            if (isHeightLitMaterial()) {
                baseTextureButton.getButton2().getParent().setVisible(terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                terrainLayersButton.getParent().setVisible(terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                baseTextureButton.getButton2().setVisible(false);
                baseTextureScale.getParent().setVisible(selectedRegion != null && terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                textureRoughness.getParent().setVisible(false);
                textureMetallic.getParent().setVisible(false);
                startHeight.getParent().setVisible(selectedRegion != null && terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                endHeight.getParent().setVisible(selectedRegion != null && terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                slopeScale.getParent().setVisible(slopeSelected && terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                paintRadius.getParent().setVisible(terrainAction.getTool() != TerrainAction.TOOL_PAINT);
                paintStrength.getParent().setVisible(terrainAction.getTool() != TerrainAction.TOOL_PAINT);
                paintScale.getParent().setVisible(terrainAction.getTool() != TerrainAction.TOOL_GRASS1
                        || terrainAction.getTool() != TerrainAction.TOOL_GRASS2
                        || terrainAction.getTool() != TerrainAction.TOOL_GRASS3);
                autoPainterButton.setVisible(false);
                autoPaintMin.getParent().setVisible(false);
                autoPaintMax.getParent().setVisible(false);
                autoPaintStartHeight.getParent().setVisible(false);

                if (selectedRegion != null) {
                    Vector3f region = terrain.getMaterial().getParamValue(selectedRegion);
                    baseTextureScale.setValue(region.z);
                    startHeight.setValue(region.x);
                    endHeight.setValue(region.y);

                } else if (slopeSelected) {
                    slopeScale.setValue(terrain.getMaterial().getParamValue("slopeTileFactor"));

                }

            } else {
                baseTextureScale.getParent().setVisible(terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                float s = terrain.getMaterial().getParamValue(selectedTextureScale);
                baseTextureScale.setValue(s * triScale);

                terrainLayersButton.getParent().setVisible(terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                baseTextureButton.getButton2().getParent().setVisible(terrainAction.getTool() == TerrainAction.TOOL_PAINT
                        || terrainAction.getTool() == TerrainAction.TOOL_GRASS1
                        || terrainAction.getTool() == TerrainAction.TOOL_GRASS2
                        || terrainAction.getTool() == TerrainAction.TOOL_GRASS3);

                textureRoughness.getParent().setVisible(isPBRMaterial() && terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                textureMetallic.getParent().setVisible(isPBRMaterial() && terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                if (isPBRMaterial()) {
                    textureRoughness.setValue(terrain.getMaterial().getParamValue(selectedRoughness));
                    textureMetallic.setValue(terrain.getMaterial().getParamValue(selectedMetallic));
                }

                startHeight.getParent().setVisible(false);
                endHeight.getParent().setVisible(false);
                slopeScale.getParent().setVisible(false);
                paintRadius.getParent().setVisible(true);
                paintStrength.getParent().setVisible(true);
                paintScale.getParent().setVisible(isObjectPaintToolSelected());

//                baseTextureButton.getButton2().setVisible(true);
                setButtonTextureFromTerrain(selectedNormalMap, baseTextureButton.getButton2());

                autoPainterButton.setVisible(terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                autoPaintMin.getParent().setVisible(terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                autoPaintMax.getParent().setVisible(terrainAction.getTool() == TerrainAction.TOOL_PAINT);
                autoPaintStartHeight.getParent().setVisible(terrainAction.getTool() == TerrainAction.TOOL_PAINT);

            }

            setButtonTextureFromTerrain(selectedDiffuseMap, baseTextureButton.getButton1());

            baseModelButton.getParent().setVisible(terrainAction.getTool() == TerrainAction.TOOL_TREES1);

            if (terrainAction.getTool() == TerrainAction.TOOL_GRASS1) {
                BatchNode grass1Node = (BatchNode) terrain.getChild(TerrainAction.BATCH_GRASS1);
                Geometry grass1 = grass1Node.getUserData(EditorUtils.MODEL);
                setButtonTextureFromMaterial(grass1.getMaterial(), "DiffuseMap", baseTextureButton.getButton1());
                selectedBatchLayer = grass1Node;

                baseTextureButton.getButton1().setId("DiffuseMap");
                baseTextureButton.getButton2().setId("NormalMap");
                
                window.getApplication().getMessageManager().sendMessage(Action.TERRAIN_MODEL_EDIT, grass1);

            } else if (terrainAction.getTool() == TerrainAction.TOOL_GRASS2) {
                BatchNode grass2Node = (BatchNode) terrain.getChild(TerrainAction.BATCH_GRASS2);
                Geometry grass2 = grass2Node.getUserData(EditorUtils.MODEL);
                setButtonTextureFromMaterial(grass2.getMaterial(), "DiffuseMap", baseTextureButton.getButton1());
                selectedBatchLayer = grass2Node;

                baseTextureButton.getButton1().setId("DiffuseMap");
                baseTextureButton.getButton2().setId("NormalMap");
                
                window.getApplication().getMessageManager().sendMessage(Action.TERRAIN_MODEL_EDIT, grass2);

            } else if (terrainAction.getTool() == TerrainAction.TOOL_GRASS3) {
                BatchNode grass3Node = (BatchNode) terrain.getChild(TerrainAction.BATCH_GRASS3);
                Geometry grass3 = grass3Node.getUserData(EditorUtils.MODEL);
                setButtonTextureFromMaterial(grass3.getMaterial(), "DiffuseMap", baseTextureButton.getButton1());
                selectedBatchLayer = grass3Node;

                baseTextureButton.getButton1().setId("DiffuseMap");
                baseTextureButton.getButton2().setId("NormalMap");
                
                window.getApplication().getMessageManager().sendMessage(Action.TERRAIN_MODEL_EDIT, grass3);

            } else if (terrainAction.getTool() == TerrainAction.TOOL_TREES1) {
                BatchNode trees1Node = (BatchNode) terrain.getChild(TerrainAction.BATCH_TREES1);
                Spatial tree1 = trees1Node.getUserData(EditorUtils.MODEL);
//                setButtonTextureFromMaterial(grass3.getMaterial(), "DiffuseMap", baseTextureButton.getButton1());
                selectedInstancedNode = trees1Node;

//                baseTextureButton.getButton1().setId("DiffuseMap");
//                baseTextureButton.getButton2().setId("NormalMap");

                window.getApplication().getMessageManager().sendMessage(Action.TERRAIN_MODEL_EDIT, tree1);
                
            } else {
                window.getApplication().getMessageManager().sendMessage(Action.TERRAIN_MODEL_CLEAR, null);
            }

        }

        flowPanel.layout();
    }

    private boolean isPaintableMaterial() {
        return terrain != null && terrain.getMaterial().getMaterialDef().getAssetName().endsWith("/TerrainLighting.j3md");
    }

    private boolean isPBRMaterial() {
        return terrain != null && terrain.getMaterial().getMaterialDef().getAssetName().endsWith("/PBRTerrain.j3md");
    }

    private boolean isHeightLitMaterial() {
        return terrain != null && terrain.getMaterial().getMaterialDef().getAssetName().endsWith("/HeightBasedTerrainLighting.j3md");
    }

    private void setButtonTextureFromTerrain(String materialTextureName, TouchButton button) {
        MatParamTexture mpt = terrain.getMaterial().getTextureParam(materialTextureName);
        Texture texture = mpt.getTextureValue();
        button.getPicture().getMaterial().setTexture("Texture", texture);

    }

    private void setButtonTextureFromMaterial(Material material, String materialTextureName, TouchButton button) {
        MatParamTexture mpt = material.getTextureParam(materialTextureName);
        Texture texture = mpt.getTextureValue();
        button.getPicture().getMaterial().setTexture("Texture", texture);

    }

    public TerrainQuad getTerrain() {
        return terrain;
    }

    public void setTerrain(TerrainQuad terrain) {
        System.out.println("Set terrain: " + terrain);
        this.terrain = terrain;

        if (isHeightLitMaterial()) {
            terrainLayersButton.setOptions(terrainHeightBasedTextureLayers);
            terrainToolsButton.setOptions(terrainToolTypesHeight);

        } else {
            terrainLayersButton.setOptions(terrainTextureLayers);
            terrainToolsButton.setOptions(terrainToolTypesPaint);

            if (isPBRMaterial()) {
                selectedDiffuseMap = "AlbedoMap_0";
                selectedNormalMap = "NormalMap_0";
                selectedTextureScale = "AlbedoMap_0_scale";

            } else {
                selectedDiffuseMap = "DiffuseMap";
                selectedNormalMap = "NormalMap";
                selectedTextureScale = "DiffuseMap_0_scale";

            }

        }

        this.reload();
    }

    public void addHeightMapButtonListener(TouchButtonListener buttonListener) {
        this.heightMapButton.addTouchButtonListener(buttonListener);

    }

    public void setHeightmapTexture(Texture texture) {
        heightMapButton.getPicture().getMaterial().setTexture("Texture", texture);
        terrainAction.setHeightMapTexture(texture);

    }

    public void setTerrainMaterialTexture(String name, Texture texture) {

        if (terrain != null && name != null && texture != null) {
            texture.setWrap(Texture.WrapMode.Repeat);
            terrain.getMaterial().setTexture(name, texture);
            reload();

        }

    }

    public void addTerrainTexturesButtonListener(TouchButtonListener buttonListener) {
        this.baseTextureButton.getButton1().addTouchButtonListener(buttonListener);
        this.baseTextureButton.getButton2().addTouchButtonListener(buttonListener);

    }

    public void addTerrainModelButtonListener(TouchButtonListener buttonListener) {
        this.baseModelButton.addTouchButtonListener(buttonListener);
    }

    public int getSelectedLayer() {
        return terrainLayersButton.getIndex();
    }

    public boolean isPaintable() {
        return terrain != null && (isPaintableMaterial() || isPBRMaterial());
    }

    public float getPaintRadius() {
        return paintRadius.getValue();
    }

    public float getPaintStrength() {
        return paintStrength.getValue();
    }

    public boolean isTerrainTriplanar() {

        if (terrain != null && terrain.getMaterial().getParamValue("useTriPlanarMapping") != null) {
            Boolean tri = terrain.getMaterial().getParamValue("useTriPlanarMapping");
            if (Boolean.TRUE.equals(tri)) {
                return true;

            }

        }

        return false;
    }

    public TerrainAction getTerrainAction() {
        return terrainAction;
    }

    public void setGrassTexture(String textureMapName, Texture texture, int grassTool, String batchName) {
        if (terrain != null && texture != null) {

            if (terrainAction.getTool() == grassTool) {
                BatchNode grass1Node = (BatchNode) terrain.getChild(batchName);
                Geometry grass1 = grass1Node.getUserData(EditorUtils.MODEL);
                Material m = grass1.getMaterial();
                texture.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
                m.setTexture(textureMapName, texture);
                setButtonTextureFromMaterial(m, textureMapName, baseTextureButton.getButton1());
                MaterialUtils.convertTextureToEmbeddedByName(m, "DiffuseMap");

                grass1Node.depthFirstTraversal(new SceneGraphVisitorAdapter() {
                    @Override
                    public void visit(Geometry geom) {
                        //2022-12-11: Set the texture on each material of each grass geometry
                        geom.getMaterial().setTexture(textureMapName, texture);
                        MaterialUtils.convertTextureToEmbeddedByName(geom.getMaterial(), "DiffuseMap");

                    }

                });

                grass1Node.batch();

            }

        }

    }

    public void setInstanceModel(Spatial spatial) {
        System.out.println("Setting instance model: " + spatial.getName());

        if (selectedInstancedNode != null) {

            //MaterialUtils.setInstancingOnAllMaterials(spatial);
            MaterialUtils.convertTexturesToEmbedded(spatial);
            selectedInstancedNode.setUserData(EditorUtils.MODEL, spatial);

//            List<Spatial> removeList = new ArrayList<>();
            List<Spatial> addList = new ArrayList<>();

            for (int i = 0; i < selectedInstancedNode.getQuantity(); i++) {
                Spatial removedObj = selectedInstancedNode.getChild(i);
//                removeList.add(removedObj);

                Spatial clone = spatial.clone(false);
                clone.setLocalRotation(removedObj.getLocalRotation().clone());
                clone.setLocalScale(removedObj.getLocalScale().clone());
                clone.setLocalTranslation(removedObj.getLocalTranslation().clone());
                addList.add(clone);

            }

//            for (int i = 0; i < removeList.size(); i++) {
//                Spatial rem = removeList.get(i);
//                if (rem.getParent() != null) {
//                    rem.removeFromParent();
//                }
//
//            }
            selectedInstancedNode.removeFromParent();
            BatchNode instancedNode = new BatchNode(selectedInstancedNode.getName());
            instancedNode.setQueueBucket(RenderQueue.Bucket.Transparent);
            instancedNode.setLocalScale(selectedInstancedNode.getLocalScale().clone());
            instancedNode.setUserData(EditorUtils.MODEL, spatial);
            terrain.attachChild(instancedNode);
            selectedInstancedNode = instancedNode;

            for (int i = 0; i < addList.size(); i++) {
                Spatial add = addList.get(i);
                selectedInstancedNode.attachChild(add);

            }

            selectedInstancedNode.batch();

        }

    }

    public Geometry getSelectedGrass() {
        return selectedBatchLayer.getUserData(EditorUtils.MODEL);
    }

    public BatchNode getSelectedBatchLayer() {
        return selectedBatchLayer;
    }

    public BatchNode getSelectedInstancedNode() {
        return selectedInstancedNode;
    }

    public Spatial getSelectedModel() {
        return selectedInstancedNode.getUserData(EditorUtils.MODEL);
    }

    private boolean isObjectPaintToolSelected() {
        return terrainAction.getTool() == TerrainAction.TOOL_GRASS1
                        || terrainAction.getTool() == TerrainAction.TOOL_GRASS2
                        || terrainAction.getTool() == TerrainAction.TOOL_GRASS3
                        || terrainAction.getTool() == TerrainAction.TOOL_TREES1;
    }

}
