package com.bluejungle.pf.domain.epicenter.evaluation;




// Copyright Blue Jungle, Inc.

/*
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/epicenter/evaluation/IEvaluatableNode.java#1 $
 */

public interface IEvaluatableNode {

    IEvaluatableNode[] getChildren();
    boolean isLeafNode();

}
