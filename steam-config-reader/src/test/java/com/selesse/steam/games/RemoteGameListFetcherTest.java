package com.selesse.steam.games;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.selesse.files.RuntimeExceptionFiles;
import com.selesse.os.Resources;
import com.selesse.steam.SteamAccountId;
import com.selesse.steam.TestGames;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

public class RemoteGameListFetcherTest {
    @Test
    public void privateProfile_returnsNoGames() throws Exception {
        SteamAccountId accountId = new SteamAccountId("76561197960287930");
        RemoteGameListFetcher remoteGameListFetcher = spy(new RemoteGameListFetcher(accountId));

        doReturn(getTestGameList(accountId)).when(remoteGameListFetcher).getOutputFromHttpConnection(anyString());

        List<Long> steamGames = remoteGameListFetcher.fetchGameIdList();
        assertThat(steamGames).isEmpty();
    }

    @Test
    public void publicProfile_canFetchGames() throws Exception {
        SteamAccountId accountId = new SteamAccountId("1234");
        RemoteGameListFetcher remoteGameListFetcher = spy(new RemoteGameListFetcher(accountId));

        doReturn(getTestGameList(accountId)).when(remoteGameListFetcher).getOutputFromHttpConnection(anyString());

        List<Long> steamGames = remoteGameListFetcher.fetchGameIdList();
        assertThat(steamGames)
                .hasSize(445)
                .contains(TestGames.HOLLOW_KNIGHT.getGameId())
                .contains(TestGames.OXYGEN_NOT_INCLUDED.getGameId());
    }

    @Test
    public void privateProfileV2_returnsNoGames() throws IOException, InterruptedException {
        SteamAccountId accountId = new SteamAccountId("4567");
        RemoteGameListFetcher remoteGameListFetcher = spy(new RemoteGameListFetcher(accountId));

        doReturn(getTestGameList(accountId)).when(remoteGameListFetcher).getOutputFromHttpConnection(anyString());

        List<Long> steamGames = remoteGameListFetcher.fetchGameIdList();
        assertThat(steamGames).isEmpty();
    }

    private String getTestGameList(SteamAccountId accountId) {
        Path resource = Resources.getResource("game-list/%s.xml".formatted(accountId.to64Bit()));
        return String.join("\n", RuntimeExceptionFiles.readAllLines(resource));
    }
}
