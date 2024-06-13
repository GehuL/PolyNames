window.addEventListener("load",run)

function run(){
    document.getElementById("role_swap").addEventListener("click",()=>{
        roleSwap();

    })
    document.getElementById("start").addEventListener("click",()=>{
        start();
    })
    const data=localStorage.getItem("game_data")
    const gameCode=JSON.parse(data).code
    document.getElementById("room").innerHTML="ROOM "+gameCode


}


// change de role, c'est a dire intervertit les role si il y a deux joueurs, ou change simplement le role si un seul joueur est dans la partie
async function roleSwap(){
    const data=localStorage.getItem("game_data")
    const id_partie=JSON.parse(data).id;


    const role= await fetch("http://localhost:8080/role/swap/"+id_partie,{method:"post"})

    if(role.status==200){
        let role_payload =await role.json()
        console.log(role_payload)
        if(role_payload.length==2){
            if(role_payload[0].role=="MAITRE_MOT"){
                document.getElementById("words_master").innerHTML=role_payload[0].nom
                document.getElementById("intuition_master").innerHTML=role_payload[1].nom
            }
            else{
                document.getElementById("words_master").innerHTML=role_payload[1].nom
                document.getElementById("intuition_master").innerHTML=role_payload[0].nom
            }
        }
        if(role_payload.length==1){
            if(role_payload[0].role=="MAITRE_MOT"){
                document.getElementById("words_master").innerHTML=role_payload[0].nom
                document.getElementById("intuition_master").innerHTML=null
        }
        else{
            document.getElementById("words_master").innerHTML=null
            document.getElementById("intuition_master").innerHTML=role_payload[0].nom
        }
    }
    }

    if(role.status==500){
        alert("ca n a pas marche ")
    }   
}


async function start(){
    const data=localStorage.getItem("game_data")
    const id_partie=JSON.parse(data).id;
    const players= await fetch("http://localhost:8080/players/"+id_partie)
    console.log(await players.json())
}