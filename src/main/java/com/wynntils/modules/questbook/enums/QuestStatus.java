/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.modules.questbook.enums;

public enum QuestStatus {

    STARTED(0), CAN_START(1), CANNOT_START(2), COMPLETED(3);

    int order;

    QuestStatus(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

}
