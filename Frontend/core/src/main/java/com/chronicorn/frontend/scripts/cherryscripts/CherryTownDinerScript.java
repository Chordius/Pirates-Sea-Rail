package com.chronicorn.frontend.scripts.cherryscripts;

import com.chronicorn.frontend.eventcommands.*;
import com.chronicorn.frontend.managers.eventManagers.EventManager;
import com.chronicorn.frontend.managers.eventManagers.GameSession;
import com.chronicorn.frontend.managers.mapManager.LevelMapManager;
import com.chronicorn.frontend.scripts.MapScript;

public class CherryTownDinerScript implements MapScript {

    @Override
    public void onMapLoad(EventManager events) {
    }

    @Override
    public void onTrigger(String triggerName, EventManager events) {
        if (triggerName.equals("Exit")) {
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 2f));
            events.queue(new CmdTransferPlayer("CherryTown", 40, 30));
            events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 2f));
        }

        // ==========================================
        // Cutscene
        // ==========================================
        else if (triggerName.equals("Cutscene_Start")) {
            if (GameSession.getInstance().isSet("DINER_SCENE")) {
                // Setup
                events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 1f));
                events.queue(new CmdFlagSet("AUTOSAVE_ENABLED", false));
                events.queue(new CmdAddParty("C002"));

                // Place the Player initially walking up to Perry and her sister
                events.queue(new CmdTransferPlayer("CherryTownDiner", 16, 13));
                events.queue(new CmdZoomCamera(0.7f, 0f));
                events.queue(new CmdScrollCamera(true, 1f));
                events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), true, 1f));

                // Make him slow a bit, as he moves up, the camera pans to Perry and her sister.
                events.queue(new CmdSetBaseSpeed("Player", 50f));
                events.queue(new CmdMoveEntity("Player", "UP", 1, true));
                events.queue(new CmdScrollCamera(0, 4, 0.1f, false));
                // The sister is surprised, while the brother is excited
                events.queue(new CmdShowBalloon("NPC_Girl", "SURPRISED", true));
                events.queue(new CmdMoveEntity("NPC_Boy", "TURN_DOWN", 0, true));
                events.queue(new CmdShowBalloon("NPC_Boy", "DELIGHTED", true));
                events.queue(new CmdSetSteppingAnimation("NPC_Boy", false));

                events.queue(new CmdShowText(
                    "Perry",
                    "Aha! You're here, mister!"
                ));

                events.queue(new CmdScrollCamera(0, -4, 0.1f, false));
                events.queue(new CmdSetSteppingAnimation("NPC_Boy", true));
                events.queue(new CmdMoveEntity("NPC_Boy", "DOWN", 3, true));
                events.queue(new CmdMoveEntity("NPC_Boy", "TURN_RIGHT", 0, true));
                events.queue(new CmdMoveEntity("Player", "TURN_LEFT", 0, true));


                events.queue(new CmdShowText(
                    "Perry",
                    "Haha, looks like YOU'RE the loser this time!"
                ));

                events.queue(new CmdSetSteppingAnimation("NPC_Boy", false));
                events.queue(new CmdShowBalloon("Player", "COBWEB", true));
                events.queue(new CmdSetBaseSpeed("Player", 70f));
                events.queue(new CmdScrollCamera(0, 4, 0.05f, false));
                events.queue(new CmdMoveEntity("NPC_Boy", "UP", 3, false));
                events.queue(new CmdMoveEntity("Player", "UP", 3, true));

                events.queue(new CmdShowBalloon("NPC_Girl", "WORRIED", true));

                events.queue(new CmdShowText(
                    "Girl",
                    "Perry, please...\\! Don't tease our guest the moment he walks in.\\! " +
                        "I'm sorry about him. He lacks manners."
                ));

                events.queue(new CmdMoveEntity("NPC_Girl", "TURN_DOWN", 0, true));
                events.queue(new CmdSetSteppingAnimation("NPC_Girl", true));
                events.queue(new CmdShowText(
                    "Cheryl",
                    "I am Cheryl. I run this diner.\\!\n" +
                        "I was so relieved when the others said you were breathing."
                ));
                events.queue(new CmdSetSteppingAnimation("NPC_Girl", false));

                // Sailor gestures thanks
                events.queue(new CmdShowBalloon("Player", "DELIGHTED", true));
                events.queue(new CmdShowBalloon("NPC_Girl", "SURPRISED", true));
                events.queue(new CmdShowBalloon("NPC_Girl", "WORRIED", false));

                events.queue(new CmdShowText(
                    "Cheryl",
                    "Oh, please, no need to thank me! Anyone would have done the same."
                ));

                events.queue(new CmdShowBalloon("Player", "IDEA", true));
                events.queue(new CmdShowBalloon("NPC_Girl", "QUESTION", true));
                events.queue(new CmdShowText(
                    "Cheryl",
                    "Anything you can help with...\\! to pay us back..."
                ));
                events.queue(new CmdSetSteppingAnimation("NPC_Girl", true));
                events.queue(new CmdShowText(
                    "Cheryl",
                    "Please, you don't need to bother yourself like that...\\! You've just woken up, after all."
                ));
                events.queue(new CmdSetSteppingAnimation("NPC_Girl", false));

                events.queue(new CmdWait(0.2f));
                events.queue(new CmdShowBalloon("NPC_Girl", "SILENCE", true));
                events.queue(new CmdWait(0.2f));

                events.queue(new CmdShowText(
                    "Cheryl",
                    "Well...\\! But if I may ask..."
                ));

                events.queue(new CmdSetSteppingAnimation("NPC_Boy", true));
                events.queue(new CmdMoveEntity("NPC_Boy", "TURN_RIGHT", 0, true));
                events.queue(new CmdMoveEntity("Player", "TURN_LEFT", 0, true));
                events.queue(new CmdShowText(
                    "Perry",
                    "Yeah, mister! Where did you sail in from?\\!\n" +
                        "Your clothes look totally different from anyone around here!"
                ));
                events.queue(new CmdShowBalloon("Player", "SILENCE", true));
                events.queue(new CmdSetSteppingAnimation("NPC_Girl", true));
                events.queue(new CmdMoveEntity("Player", "TURN_UP", 0, true));
                events.queue(new CmdShowText(
                    "Cheryl",
                    "Perry's right, your garments look quite...\\! remarkable.\\! " +
                        "And you seem to be around my age, yet you're braving the sea alone..."
                ));

                events.queue(new CmdShowBalloon("Player", "SILENCE", true));
                events.queue(new CmdShowBalloon("Player", "WORRIED", true));

                events.queue(new CmdMoveEntity("Player", "TURN_LEFT", 0, true));
                events.queue(new CmdShowText(
                    "Perry",
                    "Hey, also, also! What's your name anyway, mister?\\!\n" +
                        "You haven't told us!"
                ));

                // Intense realization of amnesia
                events.queue(new CmdShowBalloon("Player", "WORRIED", true));
                events.queue(new CmdMoveEntity("Player", "TURN_UP", 0, true));
                events.queue(new CmdWait(0.4f));
                events.queue(new CmdMoveEntity("Player", "TURN_LEFT", 0, true));
                events.queue(new CmdWait(0.4f));
                events.queue(new CmdMoveEntity("Player", "TURN_UP", 0, true));
                events.queue(new CmdWait(0.8f));

                events.queue(new CmdShowText(
                    "Sailor",
                    "...\\!\nI don't... remember."
                ));

                events.queue(new CmdSetSteppingAnimation("NPC_Boy", false));
                events.queue(new CmdSetSteppingAnimation("NPC_Girl", false));
                events.queue(new CmdShowBalloon("NPC_Boy", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Girl", "SURPRISED", true));

                events.queue(new CmdShowText(
                    "Sailor",
                    "I... can't remember anything since before I came.\\! Sorry."
                ));


                events.queue(new CmdSetSteppingAnimation("NPC_Girl", true));
                events.queue(new CmdShowText(
                    "Cheryl",
                    "H-Huh?"
                ));
                events.queue(new CmdSetSteppingAnimation("NPC_Girl", false));
                events.queue(new CmdSetSteppingAnimation("NPC_Boy", true));
                events.queue(new CmdShowText(
                    "Perry",
                    "Woah... serious amnesia!\\! Just like the theater plays!"
                ));

                events.queue(new CmdMoveEntity("Player", "TURN_LEFT", 0, false));
                events.queue(new CmdMoveEntity("NPC_Girl", "TURN_LEFT", 0, false));
                events.queue(new CmdShowBalloon("NPC_Girl", "COBWEB", true));
                events.queue(new CmdMoveEntity("NPC_Boy", "TURN_UP", 0, false));
                events.queue(new CmdShowText(
                    "Cheryl",
                    "Don't say that!"
                ));
                events.queue(new CmdMoveEntity("NPC_Girl", "TURN_DOWN", 0, false));
                events.queue(new CmdMoveEntity("Player", "TURN_UP", 0, false));
                events.queue(new CmdSetSteppingAnimation("NPC_Girl", true));
                events.queue(new CmdShowText(
                    "Cheryl",
                    "But, you really don't remember anything?\\!\n" +
                        "Not even your own name?"
                ));
                events.queue(new CmdSetSteppingAnimation("NPC_Girl", false));
                events.queue(new CmdShowBalloon("Player", "WORRIED", true));
                events.queue(new CmdMoveEntity("NPC_Boy", "TURN_RIGHT", 0, false));
                events.queue(new CmdSetSteppingAnimation("NPC_Boy", true));
                events.queue(new CmdShowText(
                    "Perry",
                        "Don't worry, mister, Cherry Town is great for starting over!\\!\nI'm sure you can settle in with my sis-"
                ));
                events.queue(new CmdSetSteppingAnimation("NPC_Boy", false));

                events.queue(new CmdShowText(
                    "???",
                    "Ah, what a absolute lovely day it is for the collection, ja?"
                ));

                // Surprise
                events.queue(new CmdMoveEntity("Player", "TURN_DOWN", 0, false));
                events.queue(new CmdMoveEntity("NPC_Boy", "TURN_DOWN", 0, false));
                events.queue(new CmdMoveEntity("NPC_Girl", "TURN_DOWN", 0, false));
                events.queue(new CmdMoveEntity("NPC_5", "TURN_LEFT", 0, false));
                events.queue(new CmdSetSteppingAnimation("NPC_5", false));
                events.queue(new CmdMoveEntity("NPC_4", "TURN_LEFT", 0, false));
                events.queue(new CmdMoveEntity("NPC_3", "TURN_RIGHT", 0, false));
                events.queue(new CmdMoveEntity("NPC_2", "TURN_RIGHT", 0, false));
                events.queue(new CmdMoveEntity("NPC_1", "TURN_RIGHT", 0, false));
                events.queue(new CmdShowBalloon("Player", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Boy", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Girl", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_5", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_4", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_3", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_2", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_1", "SURPRISED", true));

                // Trigger visibility so the three guards render at the entrance
                events.queue(new CmdFlagSet("DINER_GUARD_VISIBILITY", true));

                // Camera pans down toward the door entrance to reveal the threat
                events.queue(new CmdScrollCamera(0, -4, 0.2f, false));
                events.queue(new CmdMoveEntity("NPC_Guard_2", "UP", 1, false));
                events.queue(new CmdMoveEntity("NPC_Guard_3", "UP", 1, false));
                events.queue(new CmdMoveEntity("NPC_Guard_1", "UP", 2, true));

                // TODO: Write the rest of the scene.
                events.queue(new CmdShowText(
                    "Officer",
                    "Morgen, Inlanders!\\!\n" +
                        "It is the end of the month, which means your dues to the administration are due."
                ));
                events.queue(new CmdShowText(
                    "Officer",
                    "If you got your coins ready, already, just hand it over!\\! Let's get quick, ja?"
                ));
                events.queue(new CmdScrollCamera(0, 3, 0.05f, false));
                events.queue(new CmdMoveEntity("NPC_Guard_2", "LEFT", 4, false));
                events.queue(new CmdMoveEntity("NPC_Guard_2", "UP", 4, false));
                events.queue(new CmdMoveEntity("NPC_Guard_2", "LEFT", 1, false));
                events.queue(new CmdMoveEntity("NPC_Guard_3", "UP", 1, false));
                events.queue(new CmdMoveEntity("NPC_Guard_3", "RIGHT", 1, false));
                events.queue(new CmdMoveEntity("NPC_Guard_1", "UP", 1, true));
                events.queue(new CmdMoveEntity("Player", "RIGHT", 1, false));
                events.queue(new CmdMoveEntity("Player", "TURN_LEFT", 0, false));
                events.queue(new CmdMoveEntity("NPC_Guard_1", "UP", 1, true));
                events.queue(new CmdMoveEntity("Player", "TURN_DOWN", 0, true));

                events.queue(new CmdSetSteppingAnimation("NPC_Guard_1", true));
                events.queue(new CmdShowText(
                    "Officer",
                    "Ah, Cheryl. The diner smells as wonderful as ever.\\!\n" +
                        "I expect you have our tax ready?"
                ));
                events.queue(new CmdScrollCamera(0, 1, 0.05f, false));
                events.queue(new CmdSetSteppingAnimation("NPC_Guard_1", false));
                events.queue(new CmdSetSteppingAnimation("NPC_Girl", true));
                events.queue(new CmdShowBalloon("NPC_Girl", "WORRIED", true));
                events.queue(new CmdShowText(
                    "Cheryl",
                    "U-Umm... b-but sir! You already collected the tax two weeks ago...\\!\n" +
                        "We barely have enough grain right now."
                ));
                events.queue(new CmdScrollCamera(0, -1, 0.05f, false));
                events.queue(new CmdSetSteppingAnimation("NPC_Girl", false));
                events.queue(new CmdSetSteppingAnimation("NPC_Guard_1", true));
                events.queue(new CmdWait(1f));
                events.queue(new CmdShowText(
                    "Officer",
                    "Mmm-hmm. And you think your little diner runs without our protection?\\! " +
                        "Pay up right now, or maybe we just take this whole place apart piece by piece."
                ));
                events.queue(new CmdSetSteppingAnimation("NPC_Guard_1", false));
                events.queue(new CmdSetSteppingAnimation("NPC_Girl", true));
                events.queue(new CmdScrollCamera(0, 1, 0.2f, false));
                events.queue(new CmdShowBalloon("NPC_Girl", "WORRIED", false));
                events.queue(new CmdWait(0.8f));
                events.queue(new CmdShowBalloon("NPC_Girl", "WORRIED", true));
                events.queue(new CmdShowText(
                    "Cheryl",
                    "U-Ummm, I'm sorry, Officer.\\! Err, please wait, I'll run through what we have."
                ));
                events.queue(new CmdScrollCamera(0, -1, 0.05f, false));
                events.queue(new CmdShowBalloon("NPC_Guard_1", "DELIGHTED", true));
                events.queue(new CmdShowText(
                    "Officer",
                    "Excellent. And don't forget the little booze reward for my hard-working men, ja?\\! I'll be waiting."
                ));
                events.queue(new CmdSetSteppingAnimation("NPC_Guard_1", false));
                events.queue(new CmdShowBalloon("NPC_Boy", "ANGRY", false));
                events.queue(new CmdMoveEntity("NPC_Guard_1", "DOWN", 2, false));
                events.queue(new CmdMoveEntity("NPC_Girl", "LEFT", 2, true));
                events.queue(new CmdMoveEntity("NPC_Girl", "TURN_UP", 0, false));
                events.queue(new CmdMoveEntity("NPC_Boy", "RIGHT", 1, true));
                events.queue(new CmdMoveEntity("NPC_Boy", "TURN_DOWN", 0, true));
                events.queue(new CmdSetSteppingAnimation("NPC_Boy", true));

                events.queue(new CmdShowText(
                    "Perry",
                    "HEY!"
                ));

                events.queue(new CmdSetSteppingAnimation("NPC_Girl", false));
                events.queue(new CmdMoveEntity("NPC_4", "TURN_UP", 0, false));
                events.queue(new CmdMoveEntity("NPC_Guard_3", "TURN_LEFT", 0, false));
                events.queue(new CmdMoveEntity("NPC_Guard_2", "TURN_RIGHT", 0, false));
                events.queue(new CmdMoveEntity("NPC_Girl", "TURN_DOWN", 0, false));
                events.queue(new CmdMoveEntity("Player", "TURN_LEFT", 0, false));
                events.queue(new CmdMoveEntity("Porter", "TURN_LEFT", 0, false));
                events.queue(new CmdMoveEntity("NPC_Guard_1", "TURN_UP", 0, false));
                events.queue(new CmdShowBalloon("NPC_Girl", "SURPRISED", false));
                events.queue(new CmdShowBalloon("Player", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Guard_1", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Guard_2", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_4", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_5", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_2", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Guard_3", "SURPRISED", true));

                events.queue(new CmdShowBalloon("NPC_Boy", "ANGRY", false));
                events.queue(new CmdShowText(
                    "Perry",
                    "Hey! You can't just take our money and demand free drinks on top of it!\\!\n" +
                        "Leave my sister alone!"
                ));

                events.queue(new CmdMoveEntity("Player", "TURN_DOWN", 0, false));
                events.queue(new CmdScrollCamera(0, -2, 0.05f, false));
                events.queue(new CmdSetSteppingAnimation("NPC_Boy", false));
                events.queue(new CmdSetSteppingAnimation("NPC_Guard_1", true));
                events.queue(new CmdShowText(
                    "Colonial Sergeant",
                    "What did you just say, you little Inlander brat?\\!\n" +
                        "Someone needs to teach you some proper respect!"
                ));
                events.queue(new CmdSetBaseSpeed("Player", 200f));
                events.queue(new CmdSetBaseSpeed("NPC_Guard_1", 200f));
                events.queue(new CmdMoveEntity("Porter", "DOWN", 2, false));
                events.queue(new CmdMoveEntity("Player", "DOWN", 1, true));
                events.queue(new CmdMoveEntity("Player", "LEFT", 1, false));

                events.queue(new CmdScrollCamera(0, 2, 0.05f, false));
                events.queue(new CmdMoveEntity("NPC_Guard_1", "UP", 1, true));
                events.queue(new CmdMoveEntity("Player", "TURN_DOWN", 0, false));
                events.queue(new CmdShowBalloon("NPC_Guard_1", "SURPRISED", true));
                events.queue(new CmdScrollCamera(0, -1, 0.05f, false));
                events.queue(new CmdSetSteppingAnimation("NPC_Guard_1", false));
                events.queue(new CmdShowText(
                    "Officer",
                    "Oh? And who is this?\\!\n" +
                        "I haven't seen you in the list of townspeople before.\\! How did you get here?"
                ));
                events.queue(new CmdShowText(
                    "Officer",
                    "Ach, it doesn't matter.\\! If you stand in our way, you'll feel the taste of discipli-"
                ));
                events.queue(new CmdSetSteppingAnimation("Porter", true));
                events.queue(new CmdSetBaseSpeed("Porter", 500f));
                events.queue(new CmdMoveEntity("Porter", "LEFT", 6, true));
                events.queue(new CmdShowAnimation("NPC_Guard_1", "hit", false));
                events.queue(new CmdShowAnimation("NPC_Guard_1", "hit-fire", false));
                events.queue(new CmdShowAnimation("NPC_Guard_1", "punch-fire", false));
                events.queue(new CmdMoveEntity("NPC_Guard_1", "LEFT", 2, false));
                events.queue(new CmdMoveEntity("NPC_Guard_1", "TURN_RIGHT", 0, false));

                events.queue(new CmdMoveEntity("NPC_Guard_3", "TURN_UP", 0, false));
                events.queue(new CmdShowBalloon("Player", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Boy", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_4", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_2", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_5", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Guard_3", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Girl", "SURPRISED", true));

                events.queue(new CmdShowText(
                    "Perry",
                    "Porter!?"
                ));
                events.queue(new CmdShowBalloon("Porter", "DELIGHTED", false));
                events.queue(new CmdScrollCamera(0, -1, 0.05f, false));
                events.queue(new CmdShowText(
                    "Porter",
                    "Hell yeah!\\!\nThis is what I'm talking about!\\!\nI was getting bored in here!"
                ));
                events.queue(new CmdSetBaseSpeed("Porter", 100f));
                events.queue(new CmdMoveEntity("Porter", "LEFT", 1, true));
                events.queue(new CmdMoveEntity("Porter", "TURN_UP", 1, true));
                events.queue(new CmdScrollCamera(0, 1, 0.05f, false));
                events.queue(new CmdShowText(
                    "Porter",
                    "You're the stranded traveler, huh?\\! Nice meeting you.\\!\nWhat do you say we kick these people's butts?"
                ));
                events.queue(new CmdShowText(
                    "Porter",
                    "The townspeople here been \"don't disturb this\", \"don't disturb that\".\\! It's getting boring, you know?"
                ));
                events.queue(new CmdSetSteppingAnimation("Porter", false));
                events.queue(new CmdSetBaseSpeed("NPC_Guard_3", 100f));
                events.queue(new CmdMoveEntity("NPC_Guard_3", "UP", 1, true));
                events.queue(new CmdMoveEntity("NPC_Guard_3", "TURN_LEFT", 0, true));
                events.queue(new CmdMoveEntity("NPC_4", "TURN_LEFT", 0, false));
                events.queue(new CmdMoveEntity("Porter", "TURN_RIGHT", 0, true));
                events.queue(new CmdSetSteppingAnimation("NPC_Guard_3", true));
                events.queue(new CmdShowText(
                    "Officer",
                    "Wait, I know you!\n\\! You're \\c[YELLOW]Porter Hawks\\c[WHITE] of the Hawks Pirates!"
                ));
                events.queue(new CmdSetSteppingAnimation("NPC_Guard_3", false));
                events.queue(new CmdShowBalloon("Player", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Boy", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_4", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_2", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_5", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Guard_3", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Guard_1", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Guard_2", "SURPRISED", false));
                events.queue(new CmdShowBalloon("Porter", "DELIGHTED", false));
                events.queue(new CmdShowBalloon("NPC_Girl", "SURPRISED", true));
                events.queue(new CmdSetSteppingAnimation("Porter", true));
                events.queue(new CmdShowText(
                    "Porter",
                    "\"Hawks Pirates\"?\\! What a name I've long forgotten..."
                ));
                events.queue(new CmdSetSteppingAnimation("Porter", false));
                events.queue(new CmdSetSteppingAnimation("NPC_Guard_1", true));
                events.queue(new CmdShowText(
                    "Officer",
                    "Time to get a promotion, then!"
                ));
                events.queue(new CmdSetSteppingAnimation("NPC_Guard_1", false));
                events.queue(new CmdSetSteppingAnimation("Porter", true));
                events.queue(new CmdSetSteppingAnimation("Player", true));
                events.queue(new CmdMoveEntity("Player", "TURN_LEFT", 0, true));
                events.queue(new CmdShowText(
                    "Porter",
                    "Well, time for a fight, right, Sailor?"
                ));

                events.queue(new CmdBattle(
                    "soldier", "soldier", "soldier"
                ));
                events.queue(new CmdSetSteppingAnimation("Porter", false));
                events.queue(new CmdSetSteppingAnimation("Player", false));
                events.queue(new CmdShowAnimation("NPC_Guard_1", "hit", false));
                events.queue(new CmdShowAnimation("NPC_Guard_3", "hit", false));
                events.queue(new CmdSetBaseSpeed("NPC_Guard_1", 150f));
                events.queue(new CmdSetBaseSpeed("NPC_Guard_3", 150f));
                events.queue(new CmdMoveEntity("NPC_Guard_1", "LEFT", 2, false));
                events.queue(new CmdMoveEntity("NPC_Guard_3", "RIGHT", 2, false));
                events.queue(new CmdMoveEntity("NPC_Guard_1", "TURN_RIGHT", 0, false));
                events.queue(new CmdMoveEntity("NPC_Guard_3", "TURN_LEFT", 0, true));

                events.queue(new CmdSetSteppingAnimation("Porter", true));
                events.queue(new CmdShowText(
                    "Porter",
                    "Hey, hey?\\!\nIs that all you guys really got?"
                ));
                events.queue(new CmdShowText(
                    "Porter",
                    "Why are the village people so scared of you guys?\\! You're a bunch of wuss!"
                ));
                events.queue(new CmdSetSteppingAnimation("Porter", false));
                events.queue(new CmdSetSteppingAnimation("NPC_Boy", true));
                events.queue(new CmdScrollCamera(0, 1, 0.1f));
                events.queue(new CmdWait(0.2f));
                events.queue(new CmdShowText(
                    "Perry",
                    "Woah!\\! You guys are so cool!"
                ));
                events.queue(new CmdSetSteppingAnimation("Porter", true));
                events.queue(new CmdScrollCamera(0, -1, 0.1f));
                events.queue(new CmdMoveEntity("Porter", "TURN_UP", 0, true));
                events.queue(new CmdMoveEntity("Player", "TURN_UP", 0, true));
                events.queue(new CmdShowText(
                    "Porter",
                    "Hehe, aren't we? Now, let's get-"
                ));

                events.queue(new CmdSetSteppingAnimation("NPC_Boy", false));
                events.queue(new CmdSetSteppingAnimation("Porter", false));
                events.queue(new CmdMoveEntity("Player", "TURN_DOWN", 0, false));
                events.queue(new CmdMoveEntity("Porter", "TURN_DOWN", 0, false));
                events.queue(new CmdMoveEntity("NPC_4", "TURN_LEFT", 0, false));
                events.queue(new CmdShowBalloon("NPC_Boy", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_4", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Boy", "SURPRISED", false));
                events.queue(new CmdShowBalloon("NPC_Girl", "SURPRISED", false));
                events.queue(new CmdShowBalloon("Player", "SURPRISED", false));
                events.queue(new CmdShowBalloon("Porter", "SURPRISED", true));

                events.queue(new CmdFlagSet("DINER_GUARD_VISIBILITY_2", true));

                events.queue(new CmdScrollCamera(0, -3, 0.1f, false));
                events.queue(new CmdMoveEntity("NPC_Guard_4", "UP", 3, false));
                events.queue(new CmdMoveEntity("NPC_Guard_6", "UP", 3, false));
                events.queue(new CmdMoveEntity("NPC_Guard_5", "UP", 3, true));

                events.queue(new CmdScrollCamera(0, 2, 0.1f, false));
                events.queue(new CmdSetBaseSpeed("NPC_Guard_1", 70f));
                events.queue(new CmdSetBaseSpeed("NPC_Guard_3", 70f));
                events.queue(new CmdMoveEntity("NPC_Guard_1", "RIGHT", 2, false));
                events.queue(new CmdMoveEntity("NPC_Guard_3", "LEFT", 2, true));
                events.queue(new CmdWait(0.5f));

                events.queue(new CmdShowText(
                    "Porter",
                    "Oh..."
                ));

                events.queue(new CmdSetSteppingAnimation("NPC_Guard_5", true));
                events.queue(new CmdWait(0.5f));

                events.queue(new CmdShowText(
                    "Officer",
                    "You are now surrounded!\\! Surrender!"
                ));
                events.queue(new CmdShowBalloon("Player", "SILENCE", false));
                events.queue(new CmdShowBalloon("Porter", "SILENCE", true));
                events.queue(new CmdShowBalloon("Porter", "WORRIED", true));

                events.queue(new CmdFade(LevelMapManager.getInstance().getMapScreen(), false, 1f));
                events.queue(new CmdMoveEntity("NPC_Guard_4", "UP", 2, false));
                events.queue(new CmdMoveEntity("NPC_Guard_6", "UP", 3, true));
                events.queue(new CmdMoveEntity("NPC_Guard_4", "TURN_RIGHT", 0, false));
                events.queue(new CmdMoveEntity("NPC_Guard_6", "TURN_LEFT", 0, true));
                events.queue(new CmdWait(1f));
                events.queue(new CmdFlagSet("DINER_SCENE", false));
                events.queue(new CmdFlagSet("DINER_GUARD_VISIBILITY_2", false));
                events.queue(new CmdFlagSet("DINER_GUARD_VISIBILITY", false));
                events.queue(new CmdFlagSet("PRISON_CUTSCENE", true));
                events.queue(new CmdFlagSet("AUTOSAVE_ENABLED", true));
                events.queue(new CmdTransferPlayer("Jail-1B"));
            }
        }

        // ==========================================
        // NPCs
        // ==========================================

        else if (triggerName.equals("DinerPatron1")) {
            events.queue(new CmdShowText(
                "Hungry Patron",
                "Ah, there's nothing better than a hot meal after a long day at the docks.\\! " +
                    "The owner here really knows her way around the kitchen."
            ));
            events.queue(new CmdShowText(
                "Hungry Patron",
                "You should order the seafood stew if she has any left. It's legendary around here."
            ));
        }

        else if (triggerName.equals("DinerPatron2")) {
            events.queue(new CmdShowText(
                "Chatty regular",
                "Hey, aren't you the guy who washed up on the beach?\\! " +
                    "Man, the whole town was talking about you this morning."
            ));
            events.queue(new CmdShowText(
                "Chatty regular",
                "Glad to see you're doing well enough to look for food. Pull up a chair!"
            ));
        }

        else if (triggerName.equals("DinerPatron3")) {
            events.queue(new CmdShowText(
                "Elderly Patron",
                "It's nice to see the younger generation running this place so well.\\! " +
                    "Her brother Perry can be a bit of a handful, but he's a good kid at heart."
            ));
        }

        else if (triggerName.equals("DinerPatron4")) {
            events.queue(new CmdShowText(
                "Gourmet Traveler",
                "I pass through this island once every few months, and this diner is always my first stop.\\! " +
                    "The atmosphere is just so peaceful and normal."
            ));
        }

        // This NPC is specifically placed near Porter Hawks to set up his carefree nature before the fight
        else if (triggerName.equals("DinerPatron5")) {
            events.queue(new CmdShowText(
                "Amused Patron",
                "Look at Porter over there, drinking in the middle of the afternoon again.\\!\n" +
                    "That guy doesn't have a single care in the world."
            ));
            events.queue(new CmdShowText(
                "Amused Patron",
                "He fixed my leaky roof this morning in ten minutes flat, and all he asked for in return was a cold drink."
            ));
        }

        else if (triggerName.equals("PorterTalk")) {
            events.queue(new CmdShowBalloon("Porter", "SLEEP", true));
            events.queue(new CmdShowText(
                "Porter",
                "Zzz..."
            ));
            events.queue(new CmdShowText(
                "Looks like he's sleeping..."
            ));
        }
    }
}
