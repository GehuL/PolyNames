
function run(){
    document.getElementById("createGame").addEventListener("click",()=>{
        newGame();
    })
    document.getElementById("joinGame").addEventListener("click",()=>{
        loadGame();
    })
}


window.addEventListener("load",run)



async function newGame(){//admettons que ce ca marche(pb avec xampp)
    const game = await fetch("http://localhost:8080/createGame", {method:"put"});
    const pseudo = "toto"

    if(game.status==200){

        console.log(await game.json());
        sessionStorage.setItem("game_data",game);
        loadGame(game.code,pseudo)
        window.location.href = "http://localhost:8080/roleChoice.html";
    }
    return null;
}

async function loadGame(code,pseudo){
    code = document.getElementById("input_code").value;
    pseudo = document.getElementById("input_pseudo").value;

    const load = await fetch("http://localhost:8080/joinGame/"+code, {method:"put", body:`{"nom":"${pseudo}"}`})
    if(load.status==200){
        const sseClient =  new sseClient("http://localhost:8080");
        await sseClient.connect();  
        window.location.replace("../roleChoice.html")
    }
    return null;
}
