import searchDict from "../../renderer/db/cedict";

test("searchDict", async () => {
    console.log(await searchDict("你好"));
});
