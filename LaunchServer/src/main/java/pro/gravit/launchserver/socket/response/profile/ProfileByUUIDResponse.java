package pro.gravit.launchserver.socket.response.profile;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.events.request.ProfileByUUIDRequestEvent;
import pro.gravit.launcher.profiles.PlayerProfile;
import pro.gravit.launcher.profiles.Texture;
import pro.gravit.launchserver.auth.AuthProviderPair;
import pro.gravit.launchserver.auth.core.User;
import pro.gravit.launchserver.auth.texture.TextureProvider;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

import java.io.IOException;
import java.util.UUID;

public class ProfileByUUIDResponse extends SimpleResponse {
    public UUID uuid;
    public String client;

    @Deprecated
    public static PlayerProfile getProfile(UUID uuid, String username, String client, TextureProvider textureProvider) {
        // Get skin texture
        Texture skin;
        try {
            skin = textureProvider.getSkinTexture(uuid, username, client);
        } catch (IOException e) {
            skin = null;
        }

        // Get cloak texture
        Texture cloak;
        try {
            cloak = textureProvider.getCloakTexture(uuid, username, client);
        } catch (IOException e) {
            cloak = null;
        }

        // Return combined profile
        return new PlayerProfile(uuid, username, skin, cloak);
    }

    @Override
    public String getType() {
        return "profileByUUID";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        String username;
        AuthProviderPair pair;
        if (client.auth == null) {
            pair = server.config.getAuthProviderPair();
        } else {
            pair = client.auth;
        }
        if (pair == null) {
            sendError("ProfileByUUIDResponse: AuthProviderPair is null");
            return;
        }
        if (pair.isUseCore()) {
            User user = pair.core.getUserByUUID(uuid);
            if (user == null) {
                sendError("User not found");
                return;
            } else username = user.getUsername();
        } else {
            username = pair.handler.uuidToUsername(uuid);
            if (username == null) {
                sendError(String.format("ProfileByUUIDResponse: User with uuid %s not found or AuthProvider#uuidToUsername returned null", uuid));
                return;
            }
        }
        sendResult(new ProfileByUUIDRequestEvent(server.authManager.getPlayerProfile(pair, uuid)));
    }
}
