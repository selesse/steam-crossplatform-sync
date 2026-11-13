import { createSearchParams, useNavigate } from "react-router-dom";
import Select from "react-select";
import { useDatabase } from "../database";
import { getGameSearchOptions } from "../services/games";

export default function GameSearch() {
  const database = useDatabase();
  const searchOptions = getGameSearchOptions(database);
  const navigate = useNavigate();

  // Picking a game navigates to it rather than filling the box, so the box is
  // always empty: value={null} says so instead of clearing it after the fact.
  return (
    <div style={{ width: "50%", color: "black", margin: "auto" }}>
      <Select
        options={searchOptions}
        value={null}
        onChange={(option) => {
          if (option) {
            navigate({ pathname: "/game", search: createSearchParams({ steam_app_id: option.value }).toString() });
          }
        }}
        placeholder="Select a game"
      />
    </div>
  );
}
