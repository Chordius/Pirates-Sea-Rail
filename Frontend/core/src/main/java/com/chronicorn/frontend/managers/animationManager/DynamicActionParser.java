package com.chronicorn.frontend.managers.animationManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DynamicActionParser {

    public static Action parse(String command) {
        try {
            String cleanCmd = command.replace("Actions.", "").trim();

            // Handle empty commands safely
            if (cleanCmd.isEmpty()) return null;

            int openParen = cleanCmd.indexOf("(");
            int closeParen = cleanCmd.lastIndexOf(")");

            if (openParen == -1 || closeParen == -1) {
                Gdx.app.error("VFX", "Invalid syntax (missing parentheses): " + command);
                return null;
            }

            String methodName = cleanCmd.substring(0, openParen);
            String argString = cleanCmd.substring(openParen + 1, closeParen);

            // 1. Smart Split: Ignores commas inside nested parentheses
            List<String> rawArgs = splitArguments(argString);

            // 2. Handle Composite Actions (parallel / sequence) directly to bypass reflection vararg issues
            if (methodName.equals("parallel")) {
                ParallelAction par = Actions.parallel();
                for (String rawArg : rawArgs) {
                    par.addAction(parse(rawArg)); // Recursive call
                }
                return par;
            }
            else if (methodName.equals("sequence")) {
                SequenceAction seq = Actions.sequence();
                for (String rawArg : rawArgs) {
                    seq.addAction(parse(rawArg)); // Recursive call
                }
                return seq;
            }

            // 3. Handle Standard Actions (e.g., scaleTo, moveTo) via Reflection
            Object[] typedArgs = new Object[rawArgs.size()];
            Class<?>[] argTypes = new Class<?>[rawArgs.size()];

            for (int i = 0; i < rawArgs.size(); i++) {
                String arg = rawArgs.get(i).trim();

                if (arg.contains("(")) {
                    // It's a nested Action (e.g., color, delay)
                    typedArgs[i] = parse(arg);
                    argTypes[i] = Action.class;
                }
                else if (arg.matches("^[a-zA-Z]+[a-zA-Z0-9]*$")) {
                    // FIX: If it is alphanumeric and starts with a letter, it is an Interpolation name
                    try {
                        java.lang.reflect.Field field = com.badlogic.gdx.math.Interpolation.class.getField(arg);
                        typedArgs[i] = field.get(null);
                        argTypes[i] = com.badlogic.gdx.math.Interpolation.class;
                    } catch (Exception e) {
                        Gdx.app.error("VFX", "Unknown Interpolation or Field: " + arg);
                    }
                }
                else {
                    // Otherwise, it is a primitive parameter (float)
                    String val = arg.replace("f", "");
                    typedArgs[i] = Float.parseFloat(val);
                    argTypes[i] = float.class;
                }
            }

            Method method = Actions.class.getMethod(methodName, argTypes);
            return (Action) method.invoke(null, typedArgs);

        } catch (Exception e) {
            Gdx.app.error("VFX", "Failed to parse Action: " + command, e);
            return null;
        }
    }

    // Helper method to safely split arguments without breaking nested functions
    private static List<String> splitArguments(String argString) {
        List<String> args = new ArrayList<>();
        if (argString == null || argString.trim().isEmpty()) {
            return args;
        }

        int depth = 0;
        StringBuilder currentArg = new StringBuilder();

        for (int i = 0; i < argString.length(); i++) {
            char c = argString.charAt(i);

            if (c == '(') depth++;
            if (c == ')') depth--;

            // Only split on comma if we are not inside a nested function
            if (c == ',' && depth == 0) {
                args.add(currentArg.toString());
                currentArg.setLength(0); // Clear the buffer
            } else {
                currentArg.append(c);
            }
        }

        args.add(currentArg.toString()); // Add the final argument
        return args;
    }
}
