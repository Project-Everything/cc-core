package net.cc.core.util;

public class Constants {

    /* Permission Nodes */
    public static final String PERMISSION_COMMAND_NAME = "cc.command.name";
    public static final String PERMISSION_COMMAND_NICKNAME = "cc.command.nickname";
    public static final String PERMISSION_COMMAND_VANISH = "cc.command.vanish";

    /* Messages */
    public static final String MESSAGE_SENDER_NOT_PLAYER = "<red>Only players may use this command.</red>";

    public static final String MESSAGE_COMMAND_VANISH_ENABLE = "<gold>Vanish has been</gold> <green>enabled</green><gold>.</gold>";
    public static final String MESSAGE_COMMAND_VANISH_DISABLE = "<gold>Vanish has been</gold> <red>disabled</red><gold>.</gold>";

    public static final String MESSAGE_COMMAND_NICKNAME_VIEW = "<gold>Your current nickname is:</gold> <nickname><reset><gold>.\nSet your nickname with /nickname [nickname].</gold>";
    public static final String MESSAGE_COMMAND_NICKNAME_EMPTY = "<gold>You do not have a nickname.\nSet your nickname with /nickname [nickname].</gold>";
    public static final String MESSAGE_COMMAND_NICKNAME_RESET = "<gold>Nickname has been reset.</gold>";
    public static final String MESSAGE_COMMAND_NICKNAME_SET = "<gold>Nickname has been set to</gold> <nickname><reset><gold>.</gold>";

    public static final String MESSAGE_COMMAND_NAME_VIEW = "<gold>Your display name is:</gold> <name><reset><gold>.\nSet your display name with /displayname [name].</gold>";
    public static final String MESSAGE_COMMAND_NAME_RESET = "<gold>Display name has been reset.</gold>";
    public static final String MESSAGE_COMMAND_NAME_SET = "<gold>Display name has been set to</gold> <name><reset><gold>.</gold>";
    public static final String MESSAGE_COMMAND_NAME_INVALID = "<red>Display name does not match your player name</red>";
}
