/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bruynhuis.galago.util;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquation;
import aurelienribon.tweenengine.equations.Circ;
import com.bruynhuis.galago.app.Base3DApplication;
import com.bruynhuis.galago.control.SpatialLifeControl;
import com.bruynhuis.galago.control.camera.CameraStickControl;
import com.bruynhuis.galago.control.tween.RigidbodyAccessor;
import com.bruynhuis.galago.control.tween.SpatialAccessor;
import com.bruynhuis.galago.spatial.Road;
import com.bruynhuis.galago.sprite.Sprite;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.environment.LightProbeFactory;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.CenterQuad;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.jme3.terrain.Terrain;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.water.SimpleWaterProcessor;
import com.jme3.water.WaterFilter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * This is a spatial utility class which can be used to create or convert
 * certain spatial parameters.
 *
 * @author nidebruyn
 */
public class SpatialUtils {

    public static void updateSpatialEdge(Spatial spatial, final ColorRGBA edgeColor, final float edgeSize) {
        if (spatial != null) {
            SceneGraphVisitor sgv = new SceneGraphVisitor() {
                @Override
                public void visit(Spatial sp) {
                    if (sp instanceof Geometry) {
                        Geometry geom = (Geometry) sp;

                        MatParam param = geom.getMaterial().getParam("EdgesColor");

                        if (param != null) {
                            geom.getMaterial().setColor("EdgesColor", edgeColor);
                            geom.getMaterial().setFloat("EdgeSize", edgeSize);
                            geom.getMaterial().setBoolean("Fog_Edges", false);

                        }
                    }
                }
            };

            spatial.depthFirstTraversal(sgv);
        }

    }

    public static void updateSpatialTransparency(Spatial spatial, final boolean transparent, final float opacity) {
        if (spatial != null) {
            SceneGraphVisitor sgv = new SceneGraphVisitor() {
                @Override
                public void visit(Spatial sp) {
                    if (sp instanceof Geometry) {
                        Geometry geom = (Geometry) sp;

                        if (transparent) {
                            geom.setQueueBucket(RenderQueue.Bucket.Transparent);
                            geom.getMaterial().getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                            MatParam diffuseParam = geom.getMaterial().getParam("Diffuse");

                            if (diffuseParam == null) {
                                diffuseParam = geom.getMaterial().getParam("Color");
                            }

                            if (diffuseParam != null) {
                                ColorRGBA col = (ColorRGBA) diffuseParam.getValue();
                                diffuseParam.setValue(new ColorRGBA(col.r, col.g, col.b, opacity));
                            }

                        }
                    }
                }
            };

            spatial.depthFirstTraversal(sgv);
        }

    }

    public static float getSpatialTransparency(Spatial spatial) {
        float alpha = 1;
        if (spatial != null) {

            if (spatial instanceof Node) {
                Node node = (Node) spatial;
                return SpatialUtils.getSpatialTransparency(node.getChild(0));

            } else if (spatial instanceof Geometry) {
                Geometry geom = (Geometry) spatial;
                MatParam diffuseParam = geom.getMaterial().getParam("Diffuse");

                if (diffuseParam == null) {
                    diffuseParam = geom.getMaterial().getParam("Color");
                }

                if (diffuseParam != null) {
                    ColorRGBA col = (ColorRGBA) diffuseParam.getValue();
                    alpha = col.a;
                }
            }
        }

        return alpha;

    }

    public static void updateSpatialColor(Spatial spatial, ColorRGBA color) {
        if (spatial != null) {
            SceneGraphVisitor sgv = new SceneGraphVisitor() {
                @Override
                public void visit(Spatial sp) {
                    if (sp instanceof Geometry) {
                        Geometry geom = (Geometry) sp;
                        MatParam diffuseParam = geom.getMaterial().getParam("Diffuse");

                        if (diffuseParam == null) {
                            diffuseParam = geom.getMaterial().getParam("Color");
                        }

                        if (diffuseParam != null) {
                            diffuseParam.setValue(color);
                        }

                    }
                }
            };

            spatial.depthFirstTraversal(sgv);
        }

    }

    public static Spatial addSkySphere(Node parent, ColorRGBA bottomColor, ColorRGBA topColor, Camera camera) {

        Sphere sphere = new Sphere(20, 20, 1000, false, true);
        Geometry sky = new Geometry("sky", sphere);
        sky.setQueueBucket(RenderQueue.Bucket.Sky);
        sky.setCullHint(Spatial.CullHint.Never);
        sky.setModelBound(new BoundingSphere(Float.POSITIVE_INFINITY, Vector3f.ZERO));
        sky.addControl(new CameraStickControl(camera));

        Material m = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Resources/MatDefs/lineargradient.j3md");
        m.setColor("StartColor", topColor);
        m.setColor("EndColor", bottomColor);
        m.setFloat("MinStep", 0.2f);
        m.setFloat("MaxStep", 0.6f);
        m.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
        sky.setMaterial(m);

        rotate(sky, -90, 0, 0);

        parent.attachChild(sky);

        return sky;

    }

    /**
     *
     * @param parent
     * @param type
     * @return
     */
    public static Spatial addSkySphere(Node parent, int type, Camera camera) {
        String texture = "Resources/sky/day.jpg";

        if (type == 2) {
            texture = "Resources/sky/cloudy.jpg";

        } else if (type == 3) {
            texture = "Resources/sky/night.jpg";

        } else if (type == 4) {
            texture = "Resources/sky/dusk.jpg";

        } else if (type == 5) {
            texture = "Resources/sky/dawn.jpg";

        } else if (type == 6) {
            texture = "Resources/sky/flame.jpg";

        }

        return addSkySphere(parent, texture, camera);

    }

    /**
     * This will add a sky sphere with the given texture.
     *
     * @param parent
     * @param texture
     * @param camera
     * @return
     */
    public static Spatial addSkySphere(Node parent, String texture, Camera camera) {

        Sphere sphere = new Sphere(20, 20, 100, false, true);
        Geometry sky = new Geometry("sky", sphere);
        sky.setQueueBucket(RenderQueue.Bucket.Sky);
        sky.setCullHint(Spatial.CullHint.Never);
        sky.setModelBound(new BoundingSphere(Float.POSITIVE_INFINITY, Vector3f.ZERO));
        sky.addControl(new CameraStickControl(camera));

        Material m = addTexture(sky, texture, true);
        m.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

        rotate(sky, -90, 0, 0);

        parent.attachChild(sky);

        return sky;

    }

    /**
     * This will add a sky dome with the given texture.
     *
     * @param parent
     * @param texture
     * @param camera
     * @return
     */
    public static Spatial addSkyDome(Node parent, String texture, Camera camera) {

        Dome dome = new Dome(Vector3f.ZERO, 11, 20, 100, true);
        Geometry sky = new Geometry("sky", dome);
        sky.setQueueBucket(RenderQueue.Bucket.Sky);
        sky.setCullHint(Spatial.CullHint.Never);
        sky.setModelBound(new BoundingSphere(Float.POSITIVE_INFINITY, Vector3f.ZERO));
        sky.addControl(new CameraStickControl(camera));

        Material m = addTexture(sky, texture, true);
        m.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

//        rotate(sky, -90, 0, 0);
        parent.attachChild(sky);

        return sky;

    }

    /**
     * Add some real simple water to the scene.
     *
     * @param parent
     * @param size
     * @param yPos
     * @param waveSpeed
     * @param waterDepth
     * @return
     */
    public static SimpleWaterProcessor addSimpleWater(Node parent, Vector3f lightPos, float size, float yPos, float waveSpeed, boolean optimized) {

//        // we create a water processor
//        SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(SharedSystem.getInstance().getBaseApplication().getAssetManager());
//        waterProcessor.setReflectionScene(parent);
//
//        if (optimized) {
//            waterProcessor.setRenderSize(128, 128);
//        }
//
//        SharedSystem.getInstance().getBaseApplication().getViewPort().addProcessor(waterProcessor);
//
//        // we set the water plane
//        Vector3f waterLocation = new Vector3f(0, yPos, 0);
//        waterProcessor.setPlane(new Plane(Vector3f.UNIT_Y, waterLocation.dot(Vector3f.UNIT_Y)));
//
//        // we set wave properties
//        waterProcessor.setWaterDepth(40);         // transparency of water
//        waterProcessor.setDistortionScale(0.08f); // strength of waves
//        waterProcessor.setDistortionMix(0.1f); // strength of waves
//        waterProcessor.setWaveSpeed(waveSpeed);       // speed of waves
////        waterProcessor.setWaterTransparency(0f);
////        waterProcessor.setWaterColor(ColorRGBA.Blue);
//        waterProcessor.setReflectionClippingOffset(0);
//
//        //creating a quad to render water to
////        Quad quad = new Quad(size, size);
//        Disk disk = new Disk(7, size * 0.5f);
//        disk.scaleTextureCoordinates(new Vector2f(size / 15f, size / 15f));
//
//        //creating a geom to attach the water material
//        Geometry water = new Geometry("water", disk);
//        water.setLocalTranslation(0, yPos, 0);
//        water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
//        water.setMaterial(waterProcessor.getMaterial());
////        water.setShadowMode(RenderQueue.ShadowMode.Receive);
//        parent.attachChild(water);
        //create processor
        SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(SharedSystem.getInstance().getBaseApplication().getAssetManager());
        waterProcessor.setReflectionScene(parent);
//        waterProcessor.setDebug(true);
        SharedSystem.getInstance().getBaseApplication().getViewPort().addProcessor(waterProcessor);
        waterProcessor.setLightPosition(lightPos);

        Vector3f waterLocation = new Vector3f(0, yPos, 0);
        waterProcessor.setPlane(new Plane(Vector3f.UNIT_Y, waterLocation.dot(Vector3f.UNIT_Y)));

        //create water quad
        Geometry waterPlane = waterProcessor.createWaterGeometry(size, size);
        waterPlane.setMaterial(waterProcessor.getMaterial());
//        waterPlane.setLocalScale(40);
        waterPlane.setLocalTranslation(-size / 2, yPos, size / 2);

        parent.attachChild(waterPlane);

        return waterProcessor;

    }

    /**
     * Add more complex water.
     *
     * @param parent
     * @param lightDir
     * @param yPos
     * @return
     */
    public static FilterPostProcessor addOceanWater(Node parent, Vector3f lightDir, float waterHeight) {
        FilterPostProcessor fpp = new FilterPostProcessor(SharedSystem.getInstance().getBaseApplication().getAssetManager());

        final WaterFilter water = new WaterFilter(parent, lightDir);
//        water.setWaterTransparency(0.4f);
//        water.setShoreHardness(0.3f);
//        water.setShininess(0.01f);
//        water.setSpeed(0.5f);
//        water.setWaterColor(new ColorRGBA().setAsSrgb(0.0078f, 0.3176f, 0.5f, 1.0f));
//        water.setDeepWaterColor(new ColorRGBA().setAsSrgb(0.0039f, 0.00196f, 0.145f, 1.0f));
//        water.setUnderWaterFogDistance(80);
//        water.setWaterTransparency(0.2f);
//        water.setFoamIntensity(0.6f);        
//        water.setFoamHardness(0.5f);
//        water.setFoamExistence(new Vector3f(0.8f, 8f, 1f));
//        water.setReflectionDisplace(50);
//        water.setRefractionConstant(0.25f);
//        water.setColorExtinction(new Vector3f(30, 50, 70));
//        water.setCausticsIntensity(0.4f);        
//        water.setWaveScale(0.003f);
//        water.setMaxAmplitude(2f);
//        water.setFoamTexture((Texture2D) SharedSystem.getInstance().getBaseApplication().getAssetManager().loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
//        water.setRefractionStrength(0.2f);
        water.setWaterHeight(waterHeight);
        fpp.addFilter(water);
        SharedSystem.getInstance().getBaseApplication().getViewPort().addProcessor(fpp);
        return fpp;
    }

    /**
     * Convert the terrain to an unshaded terrain. This is for use on android
     * and slow devices.
     *
     * @param terrainQuad
     */
    public static void makeTerrainUnshaded(TerrainQuad terrainQuad) {
        SceneGraphVisitor sgv = new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                if (spatial instanceof Geometry) {
                    Geometry geom = (Geometry) spatial;

                    Material mat = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Common/MatDefs/Terrain/Terrain.j3md");
                    mat.setBoolean("useTriPlanarMapping", false);
                    mat.setTexture("Alpha", geom.getMaterial().getTextureParam("AlphaMap").getTextureValue());

                    if (geom.getMaterial().getTextureParam("DiffuseMap") != null) {
                        mat.setTexture("Tex1", geom.getMaterial().getTextureParam("DiffuseMap").getTextureValue());
                        mat.getTextureParam("Tex1").getTextureValue().setWrap(Texture.WrapMode.Repeat);
                        mat.setFloat("Tex1Scale", Float.valueOf(geom.getMaterial().getParam("DiffuseMap_0_scale").getValueAsString()));
                    }

                    if (geom.getMaterial().getTextureParam("DiffuseMap_1") != null) {
                        mat.setTexture("Tex2", geom.getMaterial().getTextureParam("DiffuseMap_1").getTextureValue());
                        mat.getTextureParam("Tex2").getTextureValue().setWrap(Texture.WrapMode.Repeat);
                        mat.setFloat("Tex2Scale", Float.valueOf(geom.getMaterial().getParam("DiffuseMap_1_scale").getValueAsString()));
                    }

                    if (geom.getMaterial().getTextureParam("DiffuseMap_2") != null) {
                        mat.setTexture("Tex3", geom.getMaterial().getTextureParam("DiffuseMap_2").getTextureValue());
                        mat.getTextureParam("Tex3").getTextureValue().setWrap(Texture.WrapMode.Repeat);
                        mat.setFloat("Tex3Scale", Float.valueOf(geom.getMaterial().getParam("DiffuseMap_2_scale").getValueAsString()));
                    }

                    geom.setMaterial(mat);

                }
            }
        };
        terrainQuad.depthFirstTraversal(sgv);
    }

    /**
     * Helper method which converts all ligting materials of a node to an
     * unshaded material.
     *
     * @param node
     */
    public static void makeUnshaded(Node node) {

        SceneGraphVisitor sgv = new SceneGraphVisitor() {
            public void visit(Spatial spatial) {

                if (spatial instanceof Geometry) {

                    Geometry geom = (Geometry) spatial;
                    Material mat = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                    Material tat = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Common/MatDefs/Terrain/Terrain.j3md");

                    if (geom.getMaterial().getTextureParam("DiffuseMap_1") != null) {

                        tat.setTexture("Alpha", geom.getMaterial().getTextureParam("AlphaMap").getTextureValue());

                        if (geom.getMaterial().getTextureParam("DiffuseMap") != null) {

                            tat.setTexture("Tex1", geom.getMaterial().getTextureParam("DiffuseMap").getTextureValue());
                            tat.getTextureParam("Tex1").getTextureValue().setWrap(Texture.WrapMode.Repeat);
                            tat.setFloat("Tex1Scale", Float.valueOf(geom.getMaterial().getParam("DiffuseMap_0_scale").getValueAsString()));

                        }

                        if (geom.getMaterial().getTextureParam("DiffuseMap_1") != null) {

                            tat.setTexture("Tex2", geom.getMaterial().getTextureParam("DiffuseMap_1").getTextureValue());
                            tat.getTextureParam("Tex2").getTextureValue().setWrap(Texture.WrapMode.Repeat);
                            tat.setFloat("Tex2Scale", Float.valueOf(geom.getMaterial().getParam("DiffuseMap_1_scale").getValueAsString()));

                        }

                        if (geom.getMaterial().getTextureParam("DiffuseMap_2") != null) {

                            tat.setTexture("Tex3", geom.getMaterial().getTextureParam("DiffuseMap_2").getTextureValue());
                            tat.getTextureParam("Tex3").getTextureValue().setWrap(Texture.WrapMode.Repeat);
                            tat.setFloat("Tex3Scale", Float.valueOf(geom.getMaterial().getParam("DiffuseMap_2_scale").getValueAsString()));

                        }

                        tat.setBoolean("useTriPlanarMapping", true);
                        geom.setMaterial(tat);

                    } else if (geom.getMaterial().getTextureParam("DiffuseMap") != null) {

                        mat.setTexture("ColorMap", geom.getMaterial().getTextureParam("DiffuseMap").getTextureValue());
                        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                        mat.setFloat("AlphaDiscardThreshold", .5f);
                        mat.setFloat("ShadowIntensity", 5);
                        mat.setVector3("LightPos", new Vector3f(5, 20, 5));
                        geom.setMaterial(mat);

                    }

                }

            }
        };

        node.depthFirstTraversal(sgv);

    }

    public static void enableWireframe(Node node, final boolean enabled) {

        SceneGraphVisitor sgv = new SceneGraphVisitor() {
            public void visit(Spatial spatial) {

                if (spatial instanceof Geometry) {

                    Geometry geom = (Geometry) spatial;
                    Material mat = geom.getMaterial();

                    if (mat != null) {
                        mat.getAdditionalRenderState().setWireframe(enabled);

                    }

                }

            }
        };

        node.depthFirstTraversal(sgv);

    }

    /**
     * Helper method which converts all materials to pixelated
     *
     * @param node
     */
    public static void makePixelated(Node node) {

        SceneGraphVisitor sgv = new SceneGraphVisitor() {
            public void visit(Spatial spatial) {

                if (spatial instanceof Geometry) {

                    Geometry geom = (Geometry) spatial;
                    if (geom.getMaterial().getTextureParam("ColorMap") != null) {
//                        System.out.println("Found colormap");
                        MatParamTexture mpt = geom.getMaterial().getTextureParam("ColorMap");
                        mpt.getTextureValue().setMinFilter(Texture.MinFilter.NearestNoMipMaps);

                    }

                }

            }
        };

        node.depthFirstTraversal(sgv);

    }

    /**
     * Adds sunlight to the scene.
     *
     * @param parent
     * @return
     */
    public static DirectionalLight addSunLight(Node parent, ColorRGBA colorRGBA) {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.85f, -0.5f)).normalizeLocal());
        sun.setColor(colorRGBA);
        sun.setFrustumCheckNeeded(true);
        parent.addLight(sun);
        /**
         * A white ambient light source.
         */
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.LightGray);
        ambient.setFrustumCheckNeeded(true);
        parent.addLight(ambient);

        return sun;
    }

    /**
     * Adds a camera node to the scene
     *
     * @param parent
     * @param camera
     * @param distance
     * @param height
     * @param angle
     * @return
     */
    public static Node addCameraNode(Node parent, Camera camera, float distance, float height, float angle) {
        final Node targetNode = new Node("camera-link");

        CameraNode cameraNode = new CameraNode("camera-node", camera);
        cameraNode.setLocalTranslation(0, height, -distance);
        cameraNode.rotate(angle * FastMath.DEG_TO_RAD, 0, 0);
        targetNode.attachChild(cameraNode);

        parent.attachChild(targetNode);

        return targetNode;
    }

    /**
     * Add a simple box to the node.
     *
     * @param parent
     * @param xExtend
     * @param yExtend
     * @param zExtend
     * @return
     */
    public static Spatial addBox(Node parent, float xExtend, float yExtend, float zExtend) {

        Box box = new Box(xExtend, yExtend, zExtend);
        Geometry geometry = new Geometry("box", box);
        parent.attachChild(geometry);
//        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
//        TangentUtils.generateBindPoseTangentsIfNecessary(box);

        return geometry;
    }
    
    /**
     * Add a simple box to the node.
     *
     * @param parent
     * @param xExtend
     * @param yExtend
     * @param zExtend
     * @return
     */
    public static Spatial addWireBox(Node parent, float xExtend, float yExtend, float zExtend) {

        WireBox box = new WireBox(xExtend, yExtend, zExtend);
        Geometry geometry = new Geometry("box", box);
        parent.attachChild(geometry);
//        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
//        TangentUtils.generateBindPoseTangentsIfNecessary(box);

        return geometry;
    }

    /**
     * Add a line to the scene
     *
     * @param parent
     * @param start
     * @param end
     * @param linewidth
     * @return
     */
    public static Spatial addLine(Node parent, Vector3f start, Vector3f end, ColorRGBA color, float linewidth) {

        Line line = new Line(start, end);
        line.setLineWidth(linewidth);
        Geometry geometry = new Geometry("line", line);
        parent.attachChild(geometry);
        geometry.setShadowMode(RenderQueue.ShadowMode.Off);

        Material m = addColor(geometry, color, true);
        m.getAdditionalRenderState().setLineWidth(linewidth);

        return geometry;
    }

    /**
     * Add a sphere to the scene.
     *
     * @param parent
     * @param zSamples
     * @param radialSamples
     * @param radius
     * @return
     */
    public static Spatial addSphere(Node parent, int zSamples, int radialSamples, float radius) {

        Sphere sphere = new Sphere(zSamples, radialSamples, radius);
//        sphere.setBound(new BoundingSphere(radius, new Vector3f(0, 0, 0)));
        Geometry geometry = new Geometry("sphere", sphere);
        parent.attachChild(geometry);
        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        return geometry;
    }

    /**
     * Add a dome to the scene.
     *
     * @param parent
     * @param zSamples
     * @param radialSamples
     * @param radius
     * @return
     */
    public static Spatial addDome(Node parent, int zSamples, int radialSamples, float radius) {

        Dome dome = new Dome(new Vector3f(0, 0, 0), zSamples, radialSamples, radius, false);
//        sphere.setBound(new BoundingSphere(radius, new Vector3f(0, 0, 0)));
        Geometry geometry = new Geometry("Dome", dome);
        parent.attachChild(geometry);
        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        return geometry;
    }

    public static Spatial addCone(Node parent, int radialSamples, float radius, float height) {

        Cylinder c = new Cylinder(2, radialSamples, 0.0001f, radius, height, true, false);
        Geometry geometry = new Geometry("cone", c);
        parent.attachChild(geometry);
        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        return geometry;
    }

    public static Spatial addDebugPoint(Node parent, float size, ColorRGBA color, Vector3f position) {
        Spatial marker = addBox(parent, size * 0.05f, size, size * 0.05f);
//        Spatial marker = addSphere(parent, 10, 10, size);
        addColor(marker, color, true);
        marker.setLocalTranslation(position.x, position.y, position.z);
        marker.move(0, size, 0);
        return marker;
    }

    public static Spatial addDebugSphere(Node parent, float size, ColorRGBA color, Vector3f position) {
        Spatial marker = addSphere(parent, 10, 10, size);
        addColor(marker, color, true);
        marker.setLocalTranslation(position.x, position.y, position.z);
        return marker;
    }

    /**
     * Add a cyclinder to the scene.
     *
     * @param parent
     * @param axisSamples
     * @param radialSamples
     * @param radius
     * @param height
     * @param closed
     * @return
     */
    public static Spatial addCylinder(Node parent, int axisSamples, int radialSamples, float radius, float height, boolean closed) {

        Cylinder cylinder = new Cylinder(axisSamples, radialSamples, radius, height, closed);
        Geometry geometry = new Geometry("cylinder", cylinder);
        parent.attachChild(geometry);
        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        return geometry;
    }
    
    /**
     * Add a simple box to the node.
     *
     * @param parent
     * @param xExtend
     * @param yExtend
     * @param zExtend
     * @return
     */
    public static Spatial addCurve(Node parent, List<Vector3f> points) {

//        Curve curve = new Curve(points, nbSubSegments);  
//        Road road = new Road(1, points);
//        Geometry geometry = new Geometry("curve", road);
//        parent.attachChild(geometry);
////        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
////        TangentUtils.generateBindPoseTangentsIfNecessary(box);
//
//        return geometry;
        return null;
    }

    /**
     * Add a simple plane to the node.
     *
     * @param parent
     * @param xExtend
     * @param zExtend
     * @return
     */
    public static Spatial addPlane(Node parent, float xExtend, float zExtend) {

        CenterQuad quad = new CenterQuad(xExtend * 2, zExtend * 2);
        Geometry geometry = new Geometry("quad", quad);
        geometry.rotate(-FastMath.DEG_TO_RAD * 90, 0, 0);
        parent.attachChild(geometry);
        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        return geometry;
    }
    
    /**
     * Add a simple sprite to the node.
     *
     * @param parent
     * @param xExtend
     * @param zExtend
     * @return
     */
    public static Spatial addCenterQuad(Node parent, float xExtend, float zExtend) {

        CenterQuad quad = new CenterQuad(xExtend * 2, zExtend * 2);
        Geometry geometry = new Geometry("sprite", quad);
        parent.attachChild(geometry);
        geometry.setShadowMode(RenderQueue.ShadowMode.Off);

        return geometry;
    }

    /**
     * Add a simple plane to the node.
     *
     * @param parent
     * @param xExtend
     * @param zExtend
     * @return
     */
    public static Spatial addQuad(Node parent, float xExtend, float zExtend) {

        Quad quad = new Quad(xExtend * 2, zExtend * 2);
        Geometry geometry = new Geometry("quad", quad);
//        geometry.rotate(-FastMath.DEG_TO_RAD * 90, 0, 0);
//        geometry.move(-xExtend, 0, zExtend);
        parent.attachChild(geometry);
        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        return geometry;
    }

    /**
     * Add a torus to the parent node
     *
     * @param parent
     * @param circleSamples
     * @param radialSamples
     * @param innerRadius
     * @param outerRadius
     * @return
     */
    public static Spatial addTorus(Node parent, int circleSamples, int radialSamples, float innerRadius, float outerRadius) {

        Torus torus = new Torus(circleSamples, radialSamples, innerRadius, outerRadius);
        Geometry geometry = new Geometry("torus", torus);
//        geometry.rotate(-FastMath.DEG_TO_RAD * 90, 0, 0);
//        geometry.move(-xExtend, 0, zExtend);
        parent.attachChild(geometry);
        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        return geometry;
    }

    /**
     * Add a sprite to the scene
     *
     * @param parent
     * @param width
     * @param height
     * @return
     */
    public static Sprite addSprite(Node parent, float width, float height) {
        Sprite sprite = new Sprite("sprite", width, height);
        parent.attachChild(sprite);

        return sprite;
    }

    /**
     * Add color to the spatial.
     *
     *
     * @param colorRGBA
     * @return
     */
    public static Material addColor(Spatial spatial, ColorRGBA colorRGBA, boolean unshaded) {
        Material material = null;

        if (unshaded) {
            material = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", colorRGBA);

        } else {
            material = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
            material.setBoolean("UseMaterialColors", true);
            material.setColor("Ambient", colorRGBA);
            material.setColor("Diffuse", colorRGBA);

        }

        spatial.setMaterial(material);

        return material;
    }

    /**
     * Add color to the spatial.
     *
     *
     * @param colorRGBA
     * @return
     */
    public static Material addColor(AssetManager assetManager, Spatial spatial, ColorRGBA colorRGBA, int type) {
        Material material = null;

        if (type == 1) {
            material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            material.setBoolean("UseMaterialColors", true);
            material.setColor("Ambient", colorRGBA);
            material.setColor("Diffuse", colorRGBA);

        } else if (type == 2) {
            material = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");
            material.setColor("BaseColor", colorRGBA);
            material.setFloat("Metallic", 0f);
            material.setFloat("Roughness", 0.5f);

            spatial.setMaterial(material);

        } else {
            material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", colorRGBA);

        }

        spatial.setMaterial(material);

        return material;
    }

    /**
     * A helper method that will update all child spatials to the new material
     *
     * @param spatial
     * @param image
     * @param baseColor
     * @param edgeColor
     * @param edgeSize
     * @param matCap
     * @param unshaded
     */
    public static void updateCartoonColor(Spatial spatial, final String image, final ColorRGBA baseColor, final ColorRGBA edgeColor, final float edgeSize, final boolean matCap, final boolean unshaded) {
        if (spatial != null) {
            SceneGraphVisitor sgv = new SceneGraphVisitor() {
                @Override
                public void visit(Spatial sp) {
                    if (sp instanceof Geometry) {
                        Geometry geom = (Geometry) sp;
                        addCartoonColor(geom, image, baseColor, edgeColor, edgeSize, matCap, unshaded);

                    }
                }
            };

            spatial.depthFirstTraversal(sgv);
        }

    }

    /**
     * Add cartoon color to the spatial.
     *
     *
     * @param colorRGBA
     * @return
     */
    public static Material addCartoonColor(Spatial spatial, String image, ColorRGBA baseColor, ColorRGBA edgeColor, float edgeSize, boolean matCap, boolean unshaded) {

        Texture texture = null;
        if (image != null) {
            texture = SharedSystem.getInstance().getBaseApplication().getAssetManager().loadTexture(image);
            texture.setWrap(Texture.WrapMode.Repeat);

        }

        Material material = null;
        if (matCap) {
            material = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Resources/MatDefs/MatCap.j3md");
            material.setColor("Multiply_Color", baseColor);
            if (texture != null) {
                material.setTexture("DiffuseMap", texture);
            }

        } else if (unshaded) {
            material = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Resources/MatDefs/UnshadedToon.j3md");
            material.setColor("Color", baseColor);
            if (texture != null) {
                material.setTexture("ColorMap", texture);

            }
        } else {
            material = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Resources/MatDefs/LightBlow.j3md");
            material.setColor("Diffuse", baseColor);
            material.setBoolean("UseMaterialColors", true);
            material.setBoolean("Multiply_Color", true);

            if (texture != null) {
                material.setTexture("DiffuseMap", texture);

            }

        }

        material.setColor("EdgesColor", edgeColor);
        material.setFloat("EdgeSize", edgeSize);
        material.setBoolean("Fog_Edges", true);

        spatial.setMaterial(material);

        return material;
    }

    public static Material addPBRColor(Spatial spatial, ColorRGBA colorRGBA) {
        Material material = null;

        material = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Common/MatDefs/Light/PBRLighting.j3md");  // create a simple material
        material.setColor("BaseColor", colorRGBA);
        material.setFloat("Metallic", 0.5f);
        material.setFloat("Roughness", 0.5f);

        spatial.setMaterial(material);

        return material;
    }

    public static Material addTexture(Spatial spatial, String texturePath, boolean unshaded) {
        return addTexture(spatial, texturePath, unshaded, false);
    }

    /**
     * Add color to the spatial.
     *
     *
     * @param colorRGBA
     * @return
     */
    public static Material addTexture(Spatial spatial, String texturePath, boolean unshaded, boolean pixelated) {
        Material material = null;

        Texture texture = SharedSystem.getInstance().getBaseApplication().getAssetManager().loadTexture(texturePath);
        texture.setWrap(Texture.WrapMode.Repeat);

        if (pixelated) {
//            texture.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
            texture.setMagFilter(Texture.MagFilter.Nearest);
        } else {
            texture.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        }

        if (unshaded) {
            material = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            material.setTexture("ColorMap", texture);

        } else {
            material = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
            material.setTexture("DiffuseMap", texture);

        }

        spatial.setMaterial(material);

        return material;
    }

    /**
     * Add color to the spatial.
     *
     *
     * @param colorRGBA
     * @return
     */
    public static Material addTextureWithHeightmap(Spatial spatial, String texturePath, String normalPath) {
        Material material = null;

        Texture texture = SharedSystem.getInstance().getBaseApplication().getAssetManager().loadTexture(texturePath);
        texture.setWrap(Texture.WrapMode.Repeat);

        Texture normal = SharedSystem.getInstance().getBaseApplication().getAssetManager().loadTexture(normalPath);
        normal.setWrap(Texture.WrapMode.Repeat);

        material = new Material(SharedSystem.getInstance().getBaseApplication().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        material.setTexture("DiffuseMap", texture);
        material.setTexture("NormalMap", normal);

        spatial.setMaterial(material);

        return material;
    }

    public static void addMaterial(Spatial spatial, Material material) {
        spatial.setMaterial(material);

    }

    /**
     * Add mass to the spatial.
     *
     * @param spatial
     * @param mass
     * @return
     */
    public static RigidBodyControl addMass(Spatial spatial, float mass) {

        if (SharedSystem.getInstance().getBaseApplication() instanceof Base3DApplication) {
            Base3DApplication base3DApplication = (Base3DApplication) SharedSystem.getInstance().getBaseApplication();

            RigidBodyControl rigidBodyControl = spatial.getControl(RigidBodyControl.class);

            if (rigidBodyControl == null) {
                CollisionShape collisionShape = null;
                if (spatial instanceof Geometry) {
                    //Check for box mesh
                    if (((Geometry) spatial).getMesh() instanceof Box) {
                        Box box = (Box) ((Geometry) spatial).getMesh();
                        collisionShape = new BoxCollisionShape(new Vector3f(box.getXExtent(), box.getYExtent(), box.getZExtent()));

                    } else if (((Geometry) spatial).getMesh() instanceof Sphere) {
                        Sphere sphere = (Sphere) ((Geometry) spatial).getMesh();
                        collisionShape = new SphereCollisionShape(sphere.getRadius());
                    }

                    //TODO: Need to check for other mesh types
                }

                if (collisionShape != null) {
                    rigidBodyControl = new RigidBodyControl(collisionShape, mass);
                } else {
                    rigidBodyControl = new RigidBodyControl(mass);
                }

                spatial.addControl(rigidBodyControl);
                base3DApplication.getBulletAppState().getPhysicsSpace().add(spatial);
            }
            rigidBodyControl.setMass(mass);

            return rigidBodyControl;

        } else {
            throw new RuntimeException("Requires a Base3DApplication implementations with physics enabled.");

        }

    }

    /**
     * Add ghost control to the spatial.
     *
     * @param spatial
     * @param mass
     * @return
     */
    public static GhostControl addGhostControl(Spatial spatial) {

        if (SharedSystem.getInstance().getBaseApplication() instanceof Base3DApplication) {
            Base3DApplication base3DApplication = (Base3DApplication) SharedSystem.getInstance().getBaseApplication();

            GhostControl control = spatial.getControl(GhostControl.class);

            if (control == null) {
                CollisionShape collisionShape = null;
                if (spatial instanceof Geometry) {
                    //Check for box mesh
                    if (((Geometry) spatial).getMesh() instanceof Box) {
                        Box box = (Box) ((Geometry) spatial).getMesh();
                        collisionShape = new BoxCollisionShape(new Vector3f(box.getXExtent(), box.getYExtent(), box.getZExtent()));

                    } else if (((Geometry) spatial).getMesh() instanceof Sphere) {
                        Sphere sphere = (Sphere) ((Geometry) spatial).getMesh();
                        collisionShape = new SphereCollisionShape(sphere.getRadius());
                    }

                    //TODO: Need to check for other mesh types
                }

                if (collisionShape != null) {
                    control = new GhostControl(collisionShape);
                }

                spatial.addControl(control);
                base3DApplication.getBulletAppState().getPhysicsSpace().add(spatial);
            }

            return control;

        } else {
            throw new RuntimeException("Requires a Base3DApplication implementations with physics enabled.");

        }

    }

    /**
     * Translate any object to a given position.
     *
     * @param spatial
     * @param x
     * @param y
     * @param z
     */
    public static void translate(Spatial spatial, float x, float y, float z) {

        if (spatial.getControl(RigidBodyControl.class) != null) {
            spatial.getControl(RigidBodyControl.class).setPhysicsLocation(new Vector3f(x, y, z));
        } else {
            spatial.setLocalTranslation(x, y, z);
        }

    }

    /**
     * Move any object with the given amount.
     *
     * @param spatial
     * @param x
     * @param y
     * @param z
     */
    public static void move(Spatial spatial, float xAmount, float yAmount, float zAmount) {

        if (spatial.getControl(RigidBodyControl.class) != null) {
            spatial.getControl(RigidBodyControl.class).setPhysicsLocation(new Vector3f(
                    spatial.getControl(RigidBodyControl.class).getPhysicsLocation().x + xAmount,
                    spatial.getControl(RigidBodyControl.class).getPhysicsLocation().y + yAmount,
                    spatial.getControl(RigidBodyControl.class).getPhysicsLocation().z + zAmount));
        } else {
            spatial.move(xAmount, yAmount, zAmount);
        }

    }

    /**
     * This helper method will interpolate a spatial to a position.
     *
     * @param spatial
     * @param x
     * @param y
     * @param z
     * @param time
     * @param delay
     */
    public static Tween interpolate(Spatial spatial, float x, float y, float z, float time, float delay, boolean loop) {
        int repeat = 0;
        if (loop) {
            repeat = Tween.INFINITY;
        }

        if (spatial.getControl(RigidBodyControl.class) == null) {
            return Tween.to(spatial, SpatialAccessor.POS_XYZ, time)
                    .target(x, y, z)
                    .delay(delay)
                    .repeatYoyo(repeat, delay)
                    .start(SharedSystem.getInstance().getBaseApplication().getTweenManager());

        } else {
            return Tween.to(spatial.getControl(RigidBodyControl.class), RigidbodyAccessor.POS_XYZ, time)
                    .target(x, y, z)
                    .delay(delay)
                    .repeatYoyo(repeat, delay)
                    .start(SharedSystem.getInstance().getBaseApplication().getTweenManager());
        }

    }

    public static Tween interpolate(Spatial spatial, float x, float y, float z, float time, float delay, boolean loop, TweenCallback callback) {
        int repeat = 0;
        if (loop) {
            repeat = Tween.INFINITY;
        }

        if (spatial.getControl(RigidBodyControl.class) == null) {
            return Tween.to(spatial, SpatialAccessor.POS_XYZ, time)
                    .target(x, y, z)
                    .delay(delay)
                    .repeatYoyo(repeat, delay)
                    .setCallback(callback)
                    .start(SharedSystem.getInstance().getBaseApplication().getTweenManager());

        } else {
            return Tween.to(spatial.getControl(RigidBodyControl.class), RigidbodyAccessor.POS_XYZ, time)
                    .target(x, y, z)
                    .delay(delay)
                    .repeatYoyo(repeat, delay)
                    .setCallback(callback)
                    .start(SharedSystem.getInstance().getBaseApplication().getTweenManager());
        }

    }

    public static void bounce(Spatial spatial, float x, float y, float z, float time, float delay, int count) {

        if (spatial.getControl(RigidBodyControl.class) == null) {
            Tween.to(spatial, SpatialAccessor.POS_XYZ, time)
                    .target(x, y, z)
                    .delay(delay)
                    .ease(Circ.OUT)
                    .repeatYoyo(count, delay)
                    .start(SharedSystem.getInstance().getBaseApplication().getTweenManager());

        } else {
            Tween.to(spatial.getControl(RigidBodyControl.class), RigidbodyAccessor.POS_XYZ, time)
                    .target(x, y, z)
                    .delay(delay)
                    .ease(Circ.OUT)
                    .repeatYoyo(count, delay)
                    .start(SharedSystem.getInstance().getBaseApplication().getTweenManager());
        }

    }

    /**
     * This method will rotate a spatial to the given angle.
     *
     * @param spatial
     * @param xAngle
     * @param yAngle
     * @param zAngle
     */
    public static void rotateTo(Spatial spatial, float xAngle, float yAngle, float zAngle) {

        float angles[] = {xAngle * FastMath.DEG_TO_RAD, yAngle * FastMath.DEG_TO_RAD, zAngle * FastMath.DEG_TO_RAD};

        if (spatial.getControl(RigidBodyControl.class) != null) {
            spatial.getControl(RigidBodyControl.class).setPhysicsRotation(new Quaternion(angles));
        } else {
            spatial.setLocalRotation(new Quaternion(angles));
        }

    }

    /**
     * This method will rotate a spatial a given amount.
     *
     * @param spatial
     * @param xAngle
     * @param yAngle
     * @param zAngle
     */
    public static void rotate(Spatial spatial, float xAngle, float yAngle, float zAngle) {

        if (spatial.getControl(RigidBodyControl.class) != null) {
            Quaternion q = spatial.getControl(RigidBodyControl.class).getPhysicsRotation();
            float angles[] = q.toAngles(null);
            angles[0] = angles[0] + xAngle * FastMath.DEG_TO_RAD;
            angles[1] = angles[1] + yAngle * FastMath.DEG_TO_RAD;
            angles[2] = angles[2] + zAngle * FastMath.DEG_TO_RAD;

            spatial.getControl(RigidBodyControl.class).setPhysicsRotation(new Quaternion(angles));

        } else {
            spatial.rotate(xAngle * FastMath.DEG_TO_RAD, yAngle * FastMath.DEG_TO_RAD, zAngle * FastMath.DEG_TO_RAD);
        }

    }

    /**
     * This helper method will slerp the spatial to a rotation.
     *
     * @param spatial
     * @param x
     * @param y
     * @param z
     * @param time
     * @param delay
     */
    public static void slerp(Spatial spatial, float xAngle, float yAngle, float zAngle, float time, float delay, boolean loop) {
        int repeat = 0;
        if (loop) {
            repeat = Tween.INFINITY;
        }

        if (spatial.getControl(RigidBodyControl.class) == null) {
            Tween.to(spatial, SpatialAccessor.ROTATION_XYZ, time)
                    .target(xAngle, yAngle, zAngle)
                    .delay(delay)
                    .repeatYoyo(repeat, delay)
                    .start(SharedSystem.getInstance().getBaseApplication().getTweenManager());

        } else {
            Tween.to(spatial.getControl(RigidBodyControl.class), RigidbodyAccessor.ROTATION_XYZ, time)
                    .target(xAngle, yAngle, zAngle)
                    .delay(delay)
                    .repeatYoyo(repeat, delay)
                    .start(SharedSystem.getInstance().getBaseApplication().getTweenManager());
        }

    }

    public static void scaleBounce(Spatial spatial, float scaleX, float scaleY, float time, float delay, boolean loop) {
        int repeat = 0;
        if (loop) {
            repeat = Tween.INFINITY;
        }

        Tween.to(spatial, SpatialAccessor.SCALE_XYZ, time)
                .target(scaleX, scaleY, 1)
                .delay(delay)
                .ease(Circ.OUT)
                .repeatYoyo(repeat, delay)
                .start(SharedSystem.getInstance().getBaseApplication().getTweenManager());
    }

    /**
     * This is a helper method that will move a spatial object in the -z
     * direction which is forward.
     *
     * @param spatial
     * @param amount
     */
    public static void forward(Spatial spatial, float amount) {

        if (spatial != null) {

            Vector3f dir = spatial.getLocalRotation().getRotationColumn(1);
            dir = dir.normalize();
//            Debug.log("forward = " + dir);
            spatial.move(amount * dir.x, amount * dir.y, amount * dir.z);

        }

    }

    /**
     * Perform the actual height modification on the terrain.
     *
     * @param worldLoc the location in the world where the tool was activated
     * @param radius of the tool, terrain in this radius will be affected
     * @param heightFactor the amount to adjust the height by
     */
    public static void doModifyTerrainHeight(Terrain terrain, Vector3f worldLoc, float radius, float heightFactor) {

        if (terrain == null) {
            return;
        }

        int radiusStepsX = (int) (radius / ((Node) terrain).getLocalScale().x);
        int radiusStepsZ = (int) (radius / ((Node) terrain).getLocalScale().z);

        float xStepAmount = ((Node) terrain).getLocalScale().x;
        float zStepAmount = ((Node) terrain).getLocalScale().z;

        List<Vector2f> locs = new ArrayList<Vector2f>();
        List<Float> heights = new ArrayList<Float>();

        for (int z = -radiusStepsZ; z < radiusStepsZ; z++) {
            for (int x = -radiusStepsZ; x < radiusStepsX; x++) {

                float locX = worldLoc.x + (x * xStepAmount);
                float locZ = worldLoc.z + (z * zStepAmount);

                // see if it is in the radius of the tool
                if (isInRadius(locX - worldLoc.x, locZ - worldLoc.z, radius)) {
                    // adjust height based on radius of the tool
                    float h = calculateHeight(radius, heightFactor, locX - worldLoc.x, locZ - worldLoc.z);
                    // increase the height
                    locs.add(new Vector2f(locX, locZ));
                    heights.add(h);
                }
            }
        }

        // do the actual height adjustment
        terrain.adjustHeight(locs, heights);

        ((Node) terrain).updateModelBound(); // or else we won't collide with it where we just edited

    }

    /**
     * See if the X,Y coordinate is in the radius of the circle. It is assumed
     * that the "grid" being tested is located at 0,0 and its dimensions are
     * 2*radius.
     *
     * @param x
     * @param z
     * @param radius
     * @return
     */
    public static boolean isInRadius(float x, float y, float radius) {
        Vector2f point = new Vector2f(x, y);
        // return true if the distance is less than equal to the radius
        return Math.abs(point.length()) <= radius;
    }

    /**
     * Interpolate the height value based on its distance from the center (how
     * far along the radius it is). The farther from the center, the less the
     * height will be. This produces a linear height falloff.
     *
     * @param radius of the tool
     * @param heightFactor potential height value to be adjusted
     * @param x location
     * @param z location
     * @return the adjusted height value
     */
    public static float calculateHeight(float radius, float heightFactor, float x, float z) {
        float val = calculateRadiusPercent(radius, x, z);
        return heightFactor * val;
    }

    public static float calculateRadiusPercent(float radius, float x, float z) {
        // find percentage for each 'unit' in radius
        Vector2f point = new Vector2f(x, z);
        float val = Math.abs(point.length()) / radius;
        val = 1f - val;
        return val;
    }

    public static Tween moveFromToCenter(Spatial spatial, float fromX, float fromY, float fromZ, float toX, float toY, float toZ, float duration, float delay) {
        SpatialUtils.translate(spatial, fromX, fromY, fromZ);

        return Tween.to(spatial, SpatialAccessor.POS_XYZ, duration)
                .target(toX, toY, toZ)
                .delay(delay);
    }

    public static Tween moveFromToCenter(Spatial spatial, float fromX, float fromY, float fromZ, float toX, float toY, float toZ, float duration, float delay, TweenCallback callback) {
        SpatialUtils.translate(spatial, fromX, fromY, fromZ);

        return Tween.to(spatial, SpatialAccessor.POS_XYZ, duration)
                .target(toX, toY, toZ)
                .delay(delay)
                .setCallback(callback);
    }

    public static Tween rotateFromTo(Spatial spatial, float fromAngle, float toAngle, float duration, float delay, TweenEquation tweenEquation, TweenCallback callback) {
        SpatialUtils.rotateTo(spatial, 0, 0, fromAngle);

        return Tween.to(spatial, SpatialAccessor.ROTATION_Z, duration)
                .target(toAngle)
                .delay(delay)
                .ease(tweenEquation)
                .setCallback(callback);
    }

    public static Tween rotateFromTo(Spatial spatial, Vector3f fromAngles, Vector3f toAngles, float duration, float delay, TweenEquation tweenEquation, TweenCallback callback) {
        SpatialUtils.rotateTo(spatial, fromAngles.x, fromAngles.y, fromAngles.z);

        return Tween.to(spatial, SpatialAccessor.ROTATION_XYZ, duration)
                .target(toAngles.x, toAngles.y, toAngles.z)
                .delay(delay)
                .ease(tweenEquation)
                .setCallback(callback);
    }

    public static Tween rotateFromTo(Spatial spatial, Vector3f fromAngles, Vector3f toAngles, float duration, float delay) {
        SpatialUtils.rotateTo(spatial, fromAngles.x, fromAngles.y, fromAngles.z);

        return Tween.to(spatial, SpatialAccessor.ROTATION_XYZ, duration)
                .target(toAngles.x, toAngles.y, toAngles.z)
                .delay(delay);
    }

    /**
     * Add text to the scene
     *
     * @param parent
     * @param font
     * @param text
     * @param size
     * @param color
     * @return
     */
    public static BitmapText addText(Node parent, BitmapFont font, String text, float size, ColorRGBA color) {
        //Add text
        BitmapText bText = new BitmapText(font);
        bText.setText(text);
        bText.setSize(size);
        bText.setColor(color);
        bText.setLocalTranslation(0, 0, 0);
        parent.attachChild(bText);
        return bText;
    }

    public static BitmapText addFadeoutText(Node parent, BitmapFont font, String text, float size, ColorRGBA color, Vector3f pos) {
        BitmapText bitmapText = addText(parent, font, text, size, color);
        bitmapText.setLocalTranslation(pos.x, pos.y, pos.z);
        bitmapText.addControl(new SpatialLifeControl(100));

        Tween.to(bitmapText, SpatialAccessor.POS_XYZ, 1.5f)
                .target(pos.x + 0.5f, pos.y + 1f, pos.z)
                .start(SharedSystem.getInstance().getBaseApplication().getTweenManager());

        bitmapText.addControl(new AbstractControl() {

            private float alpha = 1.2f;

            @Override
            protected void controlUpdate(float tpf) {

                alpha -= tpf;
                if (alpha < 0) {
                    alpha = 0;
                }

                if (alpha < 1) {
                    ((BitmapText) spatial).setAlpha(alpha);
                }

            }

            @Override
            protected void controlRender(RenderManager rm, ViewPort vp) {
            }
        });

        return bitmapText;
    }

    public static LightProbe loadLightProbe(Node parentNode, String path) {

//        Node probeNode = (Node) SharedSystem.getInstance().getBaseApplication().getAssetManager().loadModel("Models/Probes/bathroom.j3o");
        Node probeNode = (Node) SharedSystem.getInstance().getBaseApplication().getAssetManager().loadModel(path);
        LightProbe lightProbe = null;

        System.out.println("probeNode: " + probeNode);
        if (probeNode != null && probeNode.getLocalLightList().size() > 0) {
            lightProbe = (LightProbe) probeNode.getLocalLightList().get(0);
            System.out.println("Getting light probe from model");

        } else {
            lightProbe = LightProbeFactory.makeProbe(SharedSystem.getInstance().getBaseApplication().getStateManager().getState(EnvironmentCamera.class), parentNode);
            lightProbe.getArea().setRadius(200);

        }
        parentNode.addLight(lightProbe);

        System.out.println("Added probe light");

        return lightProbe;

    }

    public static Vector3f moveTowards(Vector3f start, Vector3f target, float speed) {
        Vector3f dir = target.subtract(start);
        dir = dir.normalizeLocal().mult(speed);
        return start.add(dir.x, dir.y, dir.z);

    }

    public static void changeParent(Node parent, Spatial child) {

        Transform childWorldTransform = child.getWorldTransform();

        Transform parentWorldTransform = parent.getWorldTransform();

        Transform newLocalTransform = getLocalTransformToPreserveWorldTransform(parentWorldTransform, childWorldTransform);

        parent.attachChild(child);

        child.setLocalTransform(newLocalTransform);

    }

    private static Transform getLocalTransformToPreserveWorldTransform(Transform parentTransform, Transform childTransform) {

        Vector3f scale = childTransform.getScale().divide(parentTransform.getScale());

        Quaternion rotation = parentTransform.getRotation().inverse().multLocal(childTransform.getRotation());

        Vector3f translation = parentTransform.getRotation().inverse()
                .multLocal(childTransform.getTranslation().subtract(parentTransform.getTranslation()))
                .divideLocal(parentTransform.getScale());

        return new Transform(translation, rotation, scale);

    }

    public static Node findRootNode(Spatial spatial) {
        if (spatial.getParent() != null) {
            return findRootNode(spatial.getParent());
        } else {
            return (Node) spatial;
        }
    }

    /**
     * Check if a spatial has a specific tag
     *
     * @param spatial
     * @param tag
     * @return
     */
    public static boolean hasTag(Spatial spatial, String tag) {
        if (spatial != null && tag != null) {
            String t = spatial.getUserData("TAG");
            if (t != null && t.equalsIgnoreCase(tag)) {
                return true;
            }

        }
        return false;
    }

    /**
     * For now a spatial can have only one tag
     *
     * @param spatial
     * @param tag
     */
    public static void addTag(Spatial spatial, String tag) {
        if (spatial != null && tag != null) {
            spatial.setUserData("TAG", tag);
            spatial.depthFirstTraversal(new SceneGraphVisitor() {
                @Override
                public void visit(Spatial sptl) {
                    System.out.println("Setting tag: " + tag);
                    spatial.setUserData("TAG", tag);
                }
            });
        }
    }
}
