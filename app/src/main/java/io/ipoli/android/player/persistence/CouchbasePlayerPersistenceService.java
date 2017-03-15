package io.ipoli.android.player.persistence;

import com.couchbase.lite.Database;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.player.Player;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/6/17.
 */
public class CouchbasePlayerPersistenceService extends BaseCouchbasePersistenceService<Player> implements PlayerPersistenceService {

    public CouchbasePlayerPersistenceService(Database database, ObjectMapper objectMapper) {
        super(database, objectMapper);
    }

    @Override
    protected String getPlayerId(Player obj) {
        String playerId = super.getPlayerId(obj);
        if (StringUtils.isEmpty(playerId) && obj != null) {
            return obj.getId();
        }
        return playerId;
    }

    @Override
    public Player get() {
        return toObject(database.getExistingDocument(getPlayerId()).getProperties());
    }

    @Override
    public void listen(OnDataChangedListener<Player> listener) {
        listenById(getPlayerId(), listener);
    }

    @Override
    protected Class<Player> getModelClass() {
        return Player.class;
    }
}