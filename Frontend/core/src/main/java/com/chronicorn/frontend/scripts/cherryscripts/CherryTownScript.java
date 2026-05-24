package com.chronicorn.frontend.scripts.cherryscripts;

import com.chronicorn.frontend.eventcommands.*;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.scripts.MapScript;

public class CherryTownScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        if (triggerName.equals("LocalGov_Deny")) {
            events.queue(new CmdShowBalloon("GuardNPC", "SURPRISED", true));
            events.queue(new CmdMoveEntity("GuardNPC", 1, 3, true));
            events.queue(new CmdMoveEntity("GuardNPC", 3, 1, true));
            events.queue(new CmdMoveEntity("Player", 4, 0, true));
            events.queue(new CmdShowText(
                "Anxious Citizen",
                "Hey there, traveler. I wouldn't go up those steps if I were you.\\!\n" +
                    "That's the local administration house up north."
            ));
            events.queue(new CmdShowText(
                "Anxious Citizen",
                "The officers up there usually keep to themselves, so let's keep it that way.\\! " +
                    "No need to make a ruckus or draw any unwanted attention."
            ));
            events.queue(new CmdMoveEntity("Player", 0, 1, true));
            events.queue(new CmdMoveEntity("GuardNPC", 0, 1, true));
            events.queue(new CmdMoveEntity("GuardNPC", 2, 3, true));
            events.queue(new CmdMoveEntity("GuardNPC", 5, 0, true));
        }

        else if (triggerName.equals("Exit_Town_Deny")) {
            events.queue(new CmdShowText(
                "Sailor",
                "(I shouldn't leave yet.\\! Better pay my thanks to the people here.\\! They've been so kind.)"
            ));
        }

        else if (triggerName.equals("Door_to_Inn")) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 2f));
            events.queue(new CmdTransferPlayer("CherryTownInn", 18, 20));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 2f));
        }

        else if (triggerName.equals("Door_to_Diner")) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 2f));
            events.queue(new CmdTransferPlayer("CherryTownDiner", 16, 17));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 2f));
        }

        // ==========================================
        // 1. THE NORTHERN BLOCK (Near Government Building Stairs)
        // ==========================================
        else if (triggerName.equals("GuardNPC")) {
            events.queue(new CmdShowText(
                "Anxious Citizen",
                "Hey there, traveler. I wouldn't go up those steps if I were you.\\!\n" +
                    "That's the local administration house up north."
            ));
            events.queue(new CmdShowText(
                "Anxious Citizen",
                "The officers up there usually keep to themselves, so let's keep it that way.\\! " +
                    "No need to make a ruckus or draw any unwanted attention."
            ));
        }

        // ==========================================
        // 2. THE CENTER SQUARE / GREENERY FORK (4 NPCs)
        // ==========================================
        else if (triggerName.equals("CenterMan1")) {
            events.queue(new CmdShowText(
                "Townsman",
                "Oh! You're the fellow they hauled off the sand this morning!\\!\n" +
                    "Glad to see you up and walking."
            ));
            events.queue(new CmdShowText(
                "Townsman",
                "If you're feeling lost, you should look for \\c[YELLOW]Porter\\c[WHITE].\\!\n" +
                    "He washed ashore here a year ago just like you. Total stranger, but now he's our handyman!"
            ));
        }

        else if (triggerName.equals("CenterWoman1")) {
            events.queue(new CmdShowText(
                "Townswoman",
                "We really have it good here. It's a long trek to any other town,\\!\n" +
                    "but that just means we get to live our lives in peace."
            ));
            events.queue(new CmdShowText(
                "Townswoman",
                "I'm just glad things are the way they are today. Simple and quiet."
            ));
        }

        else if (triggerName.equals("CenterElder")) {
            events.queue(new CmdShowText(
                "Elderly Man",
                "Ah, you're the one Perry's sister took in.\\!\n" +
                    "Make sure you head to the diner north of the square to thank her."
            ));
            events.queue(new CmdShowText(
                "Elderly Man",
                "She cooked up a storm worrying about whether you'd even wake up."
            ));
        }

        else if (triggerName.equals("CenterChild")) {
            events.queue(new CmdShowText(
                "Running Kid",
                "Perry told me a real traveler was staying at the inn!\\!\n" +
                    "Do you have a big boat? Have you ever fought a giant whale?"
            ));
        }

        // ==========================================
        // 3. THE WEST DOCKS (4 NPCs)
        // ==========================================
        // DockWorker1 and DockWorker2 are facing each other
        else if (triggerName.equals("DockWorker1")) {
            events.queue(new CmdShowText(
                "Fisherman",
                "I'm telling you, it's the blessing of the Sea today!\\!\n" +
                    "Look at the size of these mackerel!"
            ));
        }

        else if (triggerName.equals("DockWorker2")) {
            events.queue(new CmdShowText(
                "Dock Worker",
                "Aye, it's a fine catch. Help me move these boxes over to the port storage,\\!\n" +
                    "then we can celebrate at the diner."
            ));
        }

        else if (triggerName.equals("DockWorker3")) {
            events.queue(new CmdShowText(
                "Dock Worker",
                "Breaking up a sweat with the wind breezing and the sea whistling...\\! " +
                    "Aint it just beautiful?"
            ));
        }

        else if (triggerName.equals("DockPorterFan")) {
            events.queue(new CmdShowText(
                "Old Sailor",
                "Need something fixed around town? You'll want to find Porter."
            ));
            events.queue(new CmdShowText(
                "Old Sailor",
                "He's carefree and a bit careless with his tools, but he's incredibly reliable when it counts."
            ));
        }

        else if (triggerName.equals("DockTrader")) {
            events.queue(new CmdShowText(
                "Infrequent Trader",
                "The waters around Cherry Island are calm, but getting cargo out to the other islands is getting tougher.\\!\n" +
                    "I'm just resting my feet before the long voyage back."
            ));
        }

        // ==========================================
        // 4. NORTHWEST MURAL (1 NPC looking at the sea)
        // ==========================================
        else if (triggerName.equals("MuralWatcher")) {
            events.queue(new CmdShowText(
                "Mural Painter",
                "The sea looks endless from up here on the cliffs, doesn't it?"
            ));
            events.queue(new CmdShowText(
                "Mural Painter",
                    "Sometimes I wonder what lies past the horizon... but then I look back at our cozy town and forget all about it."
            ));
        }


        // ==========================================
        // 5. SOUTHERN RAILING / OVERLOOKING BEACH (1 NPC)
        // ==========================================
        else if (triggerName.equals("BeachWatcher")) {
            events.queue(new CmdShowText(
                "Parent",
                "The kids have been playing down there by the water all afternoon.\\!\n" +
                    "It's a nice day for it. The tide isn't too high."
            ));
        }

        // ==========================================
        // 6. BOTTOM LEFT GREENERY PATCH (1 NPC)
        // ==========================================
        else if (triggerName.equals("GreeneryGardener")) {
            events.queue(new CmdShowText(
                "Gardener",
                "Hey! You're the traveler from the shore.\\!\n" +
                    "I was one of the folks who helped carry you up to the inn this morning."
            ));
            events.queue(new CmdShowText(
                "Gardener",
                "No need to thank me. Just take it slow and enjoy the fresh air."
            ));
        }

        // ==========================================
        // 7. REMAINING AMBIENT TOWN NPCs (3 NPCs)
        // ==========================================
        else if (triggerName.equals("StreetVendor")) {
            events.queue(new CmdShowText(
                "Merchant",
                "Fresh fruit! Grown right here on Cherry Island!\\!\n" +
                    "Perfect snack for a stroll through the plaza."
            ));
        }

        else if (triggerName.equals("GossipWoman")) {
            events.queue(new CmdShowText(
                "Townswoman",
                "Did you hear? Porter lost his wrench in the bushes near the church again yesterday.\\!\n" +
                    "For a guy who helps everyone, he really can't keep track of his own things."
            ));
        }

        else if (triggerName.equals("RestingMan")) {
            events.queue(new CmdShowText(
                "Resting Citizen",
                "Ah... nothing beats a quiet afternoon sitting on a bench.\\! " +
                    "Nothing ever happens here, and that's exactly why I love it."
            ));
        }

        else if (triggerName.equals("Cheat")) {
            events.queue(new CmdFlagSet("DINER_SCENE", true));
        }
    }
}
