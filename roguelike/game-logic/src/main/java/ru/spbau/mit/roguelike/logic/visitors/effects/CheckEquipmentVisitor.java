package ru.spbau.mit.roguelike.logic.visitors.effects;

import ru.spbau.mit.roguelike.model.units.entity.StatDescriptor;
import ru.spbau.mit.roguelike.model.units.entity.WorldEntity;
import ru.spbau.mit.roguelike.model.units.item.equipment.*;
import ru.spbau.mit.roguelike.model.visitors.ItemVisitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks that entity's stat matches equipment requirements
 */
public class CheckEquipmentVisitor implements ItemVisitor {
    private final StatDescriptor entityStat;
    private final Set<Equipment> dropedEquipment = new HashSet<>();
    private int headsUsed = 0;
    private int handsForWeaponUsed = 0;
    private int handsForArmorUsed = 0;
    private int legsUsed = 0;
    private boolean isBodyUsed = false;

    public static Set<Equipment> process(WorldEntity entity) {
        if (entity.getCurrentStatDescriptor() != null) {
            CheckEquipmentVisitor visitor = new CheckEquipmentVisitor(entity.getCurrentStatDescriptor());
            entity.getEquipment().forEach(eq -> eq.accept(visitor));
            visitor.getDropedEquipment().addAll(entity.getEquipment().stream()
                    .filter(eq -> eq.getLevel() > entity.getLevel()).collect(Collectors.toList()));
            visitor.entityStat.setHandsCount(visitor.entityStat.getHeadsCount() - visitor.headsUsed);
            visitor.entityStat.setLegsCount(visitor.entityStat.getLegsCount() - visitor.legsUsed);
            visitor.entityStat.setHandsCount(visitor.entityStat.getHandsCount()
                    - Math.min(visitor.handsForArmorUsed, visitor.handsForWeaponUsed));
            return visitor.getDropedEquipment();
        }
        return Collections.emptySet();
    }

    public CheckEquipmentVisitor(StatDescriptor entityStat) {
        this.entityStat = entityStat;
    }

    public Set<Equipment> getDropedEquipment() {
        return dropedEquipment;
    }

    @Override
    public void visit(BodyArmor item) {
        if (isBodyUsed) {
            dropedEquipment.add(item);
        }
        isBodyUsed = true;
    }

    @Override
    public void visit(HandArmor item) {
        if (handsForArmorUsed >= entityStat.getHandsCount()) {
            dropedEquipment.add(item);
        }
        handsForArmorUsed++;
    }

    @Override
    public void visit(HeadArmor item) {
        if (headsUsed >= entityStat.getHeadsCount()) {
            dropedEquipment.add(item);
        }
        headsUsed++;
    }

    @Override
    public void visit(LegArmor item) {
        if (legsUsed >= entityStat.getLegsCount()) {
            dropedEquipment.add(item);
        }
        legsUsed++;
    }

    @Override
    public void visit(Weapon item) {
        if (handsForWeaponUsed >= entityStat.getHandsCount()) {
            dropedEquipment.add(item);
        }
        handsForWeaponUsed++;
    }
}
