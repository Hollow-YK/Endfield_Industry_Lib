package endfieldindustrylib.EFcontents;

import arc.graphics.Color;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import mindustry.content.TechTree.TechNode;
import mindustry.game.Objectives.Objective;
import mindustry.game.Objectives.Research;
import mindustry.ctype.UnlockableContent;
import mindustry.type.ItemStack;
import mindustry.type.Planet;

import static endfieldindustrylib.EFcontents.EFblocks.*;
import static endfieldindustrylib.EFcontents.EFitems.*;
import static mindustry.content.TechTree.*;

/**
 * 塔卫二 (taelos-II) 科技树。
 *
 * 使用 EFTechTreeNode（extends StatusEffect）作为节点配置，
 * 嵌套 lambda 表达树结构，efNode() helper 内联完成：
 *   ① 为 rewards 创建孤立 TechNode（gate 去重）
 *   ② 从 settings 恢复 discovered 状态
 *   ③ 组装 objectives + cost → 创建显示 TechNode
 */
public class EFTechTree {

    /** 孤立节点去重 */
    private static final ObjectSet<UnlockableContent> gateCreated = new ObjectSet<>();
    /** 隐藏的研究凭证 */
    private static ItemStack[] gateCost;

    // ================================================================
    //  节点初始化（需在 EFblocks.load() 之后调用）
    // ================================================================

    /**
     * 创建所有 EFTechTreeNode 实例并配置发现关系。
     * 必须在 EFblocks.load() 之后调用（确保 .rewards() 引用的 block 非 null）。
     */
    public static void initNodes() {
        protocolCore = new EFTechTreeNode("protocol-core").color(Color.valueOf("a0a0a0")).free();
        automatedIndustryComplex = new EFTechTreeNode("automated-industry-complex").color(Color.valueOf("7ec8e3")).free();
        basicAicPlan = new EFTechTreeNode("basic-aic-plan").color(Color.valueOf("5ab0d8")).free();
        basicAicI = new EFTechTreeNode("basic-aic-i").color(Color.valueOf("91d5e8")).free();
        basicAicII = new EFTechTreeNode("basic-aic-ii").color(Color.valueOf("b5e2f0")).free();
        basicAicIII = new EFTechTreeNode("basic-aic-iii").color(Color.valueOf("d4f0f7")).free();
        valleyIv = new EFTechTreeNode("valley-iv").color(Color.valueOf("8fcb9a")).free();
        theHub = new EFTechTreeNode("the-hub").color(Color.valueOf("d4b48c")).free();
        originiumSciencePark = new EFTechTreeNode("originium-science-park").color(Color.valueOf("c66322")).free();
        originLodespring = new EFTechTreeNode("origin-lodespring").color(Color.valueOf("4f7ebf")).free();
        powerPlateau = new EFTechTreeNode("power-plateau").color(Color.valueOf("f1c40f")).free();
        aburreyQuarry = new EFTechTreeNode("aburrey-quarry").color(Color.valueOf("b5977a")).free();
        valleyPass = new EFTechTreeNode("valley-pass").color(Color.valueOf("a8d5ba")).free();
        itemsCategory = new EFTechTreeNode("items-category").color(Color.valueOf("2ecc71")).free();
        miningI = new EFTechTreeNode("mining-i").color(Color.valueOf("c66322")).free().objectives(new Research(basicAicI));
        miningII = new EFTechTreeNode("mining-ii").color(Color.valueOf("a55fc4")).free().objectives(new Research(basicAicI));
        miningIII = new EFTechTreeNode("mining-iii").color(Color.valueOf("4f7ebf")).free().objectives(new Research(basicAicIII));
        solidFilling = new EFTechTreeNode("solid-filling").color(Color.valueOf("95a5a6")).free().objectives(new Research(basicAicII));
        planting = new EFTechTreeNode("planting").color(Color.valueOf("27ae60")).free().rewards(seedPickingUnit, plantingUnit).objectives(new Research(basicAicII));
        depotBus = new EFTechTreeNode("depot-bus").color(Color.valueOf("7f8c8d")).free().objectives(new Research(basicAicII));
        logisticsI = new EFTechTreeNode("logistics-i").color(Color.valueOf("7f8c8d")).free().rewards(transportBelt).objectives(new Research(basicAicI));
        itemAccessPort = new EFTechTreeNode("item-control-port").color(Color.valueOf("95a5a6")).free().rewards(itemControlPort).objectives(new Research(basicAicI));
        beltSplitting = new EFTechTreeNode("belt-splitting").color(Color.valueOf("aab7b8")).free().rewards(splitter).objectives(new Research(basicAicII));
        beltBridging = new EFTechTreeNode("belt-bridging").color(Color.valueOf("bdc3c7")).free().rewards(beltBridge).objectives(new Research(basicAicII));
        beltConverging = new EFTechTreeNode("belt-converging").color(Color.valueOf("d5dbdb")).free().rewards(converger).objectives(new Research(basicAicIII));
        refineI = new EFTechTreeNode("refine-i").color(Color.valueOf("c0392b")).free().rewards(refiningUnit).objectives(new Research(basicAicI));
        materialMoulding = new EFTechTreeNode("material-moulding").color(Color.valueOf("e74c3c")).free().rewards(mouldingUnit).objectives(new Research(basicAicI));
        grinding = new EFTechTreeNode("grinding").color(Color.valueOf("ff9300")).free().rewards(grindingUnit).objectives(new Research(basicAicIII));
        shreddingI = new EFTechTreeNode("shredding-i").color(Color.valueOf("8e44ad")).free().rewards(shreddingUnit).objectives(new Research(basicAicI));
        partsFitting = new EFTechTreeNode("parts-fitting").color(Color.valueOf("9b59b6")).free().rewards(fittingUnit).objectives(new Research(basicAicI));
        packagingTech = new EFTechTreeNode("packaging-tech").color(Color.valueOf("a569bd")).free().rewards(packagingUnit).objectives(new Research(basicAicII));
        electricityI = new EFTechTreeNode("electricity-i").color(Color.valueOf("f39c12")).free().rewards(electricPylon).objectives(new Research(basicAicI));
        powerRelay = new EFTechTreeNode("power-relay").color(Color.valueOf("e67e22")).free().rewards(relayTower).objectives(new Research(basicAicI));
        powerI = new EFTechTreeNode("power-i").color(Color.valueOf("d35400")).free().rewards(thermalBank).objectives(new Research(basicAicII));
        fieldStash = new EFTechTreeNode("field-stash").color(Color.valueOf("2c3e50")).free().rewards(protocolStash).objectives(new Research(basicAicI));
        defenseI = new EFTechTreeNode("defense-i").color(Color.valueOf("e74c3c")).free().rewards(gunTower).objectives(new Research(basicAicI));
        areaDenialI = new EFTechTreeNode("area-denial-i").color(Color.valueOf("c0392b")).free().rewards(grenadeTower).objectives(new Research(basicAicII));
        pylonRelaying = new EFTechTreeNode("pylon-relaying").color(Color.valueOf("f39c12")).free().objectives(new Research(basicAicII));
        relayRedistribution = new EFTechTreeNode("relay-redistribution").color(Color.valueOf("e67e22")).free().objectives(new Research(basicAicIII));
        customDefenseI = new EFTechTreeNode("custom-defense-i").color(Color.valueOf("c0392b")).free().rewards(heavyGunTower).objectives(new Research(basicAicIII));
        customDefenseII = new EFTechTreeNode("custom-defense-ii").color(Color.valueOf("e74c3c")).free().rewards(sentryTower).objectives(new Research(basicAicIII));
        hostileControlI = new EFTechTreeNode("hostile-control-i").color(Color.valueOf("3498db")).free().rewards(lnTower).objectives(new Research(basicAicII));
        hostileControlII = new EFTechTreeNode("hostile-control-ii").color(Color.valueOf("2980b9")).free().rewards(omnidirectionalSonicTower).objectives(new Research(basicAicIII));
        pointDefenseI = new EFTechTreeNode("point-defense-i").color(Color.valueOf("8e44ad")).free().rewards(beamTower).objectives(new Research(basicAicIII));
        areaDenialII = new EFTechTreeNode("area-denial-ii").color(Color.valueOf("d35400")).free().rewards(surgeTower).objectives(new Research(basicAicIII));

        // ===== 发现关系 =====
    }

    // ================================================================
    //  主入口
    // ================================================================

    public static void load(Planet planet) {
        gateCost = ItemStack.with(EFitems.researchGate, 1);
        gateCreated.clear();

        planet.techTree = nodeRoot("taelos-II", planet, () -> {
            efNode(automatedIndustryComplex, () -> {
                // ===== Basic AIC Plan =====
                efNode(basicAicPlan, () -> {
                    // Phase markers
                    efNode(basicAicI, () -> {
                        efNode(basicAicII, () -> {
                            efNode(basicAicIII, () -> {});
                        });
                    });
                    // Resourcing: Mining I → II → III
                    efNode(miningI, () -> {
                        efNode(miningII, () -> {
                            efNode(miningIII, () -> {});
                        });
                    });
                    // Logistics
                    efNode(logisticsI, () -> {
                        efNode(itemAccessPort, () -> {
                            efNode(beltSplitting, () -> {
                                efNode(beltBridging, () -> {
                                    efNode(beltConverging, () -> {});
                                });
                            });
                        });
                    });
                    // Processing - Top row
                    efNode(refineI, () -> {
                        efNode(materialMoulding, () -> {
                            efNode(solidFilling, () -> {
                                efNode(planting, () -> {
                                    efNode(grinding, () -> {});
                                });
                            });
                        });
                    });
                    // Processing - Bottom row
                    efNode(shreddingI, () -> {
                        efNode(partsFitting, () -> {
                            efNode(packagingTech, () -> {});
                        });
                    });
                    // Power
                    efNode(electricityI, () -> {
                        efNode(powerRelay, () -> {
                            efNode(powerI, () -> {
                                efNode(depotBus, () -> {});
                            });
                        });
                    });
                    efNode(pylonRelaying, () -> {});
                    efNode(relayRedistribution, () -> {});
                    // Combat
                    efNode(defenseI, () -> {
                        efNode(areaDenialI, () -> {
                            efNode(hostileControlI, () -> {
                                efNode(hostileControlII, () -> {});
                            });
                            efNode(customDefenseI, () -> {
                                efNode(customDefenseII, () -> {});
                                efNode(pointDefenseI, () -> {});
                            });
                            efNode(areaDenialII, () -> {});
                        });
                    });
                    efNode(fieldStash, () -> {});
                });
            });
            // Regions
            efNode(valleyIv, () -> {
                efNode(theHub, () -> {
                    efNode(originiumSciencePark, () -> {
                        efNode(originLodespring, () -> {
                            efNode(powerPlateau, () -> {});
                        });
                    });
                    efNode(valleyPass, () -> {});
                    efNode(aburreyQuarry, () -> {});
                });
            });
            efNode(itemsCategory, () -> {});
        });

        planet.techTree.planet = planet;
        EFblocks.registerToPlanet(planet);
        EFitems.registerToPlanet(planet);
    }

    // ================================================================
    //  efNode helper：配置节点 → Mindustry TechNode
    // ================================================================

    /**
     * 将一个 EFTechTreeNode 配置转换为 Mindustry TechNode。
     * 内联完成 gate 创建、objectives 组装。
     */
    private static TechNode efNode(EFTechTreeNode config, Runnable children) {
        // ① 为 rewards 创建孤立 TechNode
        for (var content : config.rewards) {
            if (gateCreated.add(content)) {
                TechNode n = new TechNode(null, content, gateCost);
                n.objectives.clear();
            }
        }

        // ② 组装 objectives 和 cost
        ItemStack[] cost = (config.researchCost == null || config.researchCost.length == 0)
            ? ItemStack.empty : config.researchCost;

        Seq<Objective> objectives = new Seq<>(config.unlockObjectives);

        // ③ 创建显示 TechNode
        return node(config, cost, objectives, children);
    }
}
