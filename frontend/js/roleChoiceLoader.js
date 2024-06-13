window.addEventListener("load",run)

function run(){
    document.getElementById("role_swap").addEventListener("click",()=>{
        roleSwap();

    })
    document.getElementById("start").addEventListener("click",()=>{
        start();
    })
    const data=localStorage.getItem("game_data")
    if(data){
        const gameCode=JSON.parse(data).code
        document.getElementById("room").innerHTML="ROOM "+gameCode
    }
    if(document.getElementById("intuition_master").innerHTML==""){
        roleSwap()//c'est pour que les deux joueurs n'atterrissent pas dans le meme role, car la valeur du role est par defaut maitre des mots 
    }
        

    


}


// change de role, c'est a dire intervertit les role si il y a deux joueurs, ou change simplement le role si un seul joueur est dans la partie
async function roleSwap(){
    const data=localStorage.getItem("pseudo")
    const id_partie=JSON.parse(data).idPartie
    const idJoueur=JSON.parse(data).id


    const role= await fetch("http://localhost:8080/role/swap/"+id_partie,{method:"post"})

    if(role.status==200){
        let role_payload_json =await role.json()
        /*let role_payload_txt=JSON.stringify(role_payload_json[0])
        console.log(JSON.parse(role_payload_txt))
        console.log(role_payload_json[0])*/
        if(role_payload_json.length==2){
            if(role_payload_json[0].role=="MAITRE_MOT"){
                document.getElementById("words_master").innerHTML=role_payload_json[0].nom
                document.getElementById("intuition_master").innerHTML=role_payload_json[1].nom
            }
            else{
                document.getElementById("words_master").innerHTML=role_payload_json[1].nom
                document.getElementById("intuition_master").innerHTML=role_payload_json[0].nom
            }
        }
        if(role_payload_json.length==1){
            if(role_payload_json[0].role=="MAITRE_MOT"){
                document.getElementById("words_master").innerHTML=role_payload_json[0].nom
                document.getElementById("intuition_master").innerHTML=null
        }
        else{
            document.getElementById("words_master").innerHTML=null
            document.getElementById("intuition_master").innerHTML=role_payload_json[0].nom
        }
    }
    for(let i=0;i<role_payload_json.length;i++){
        if(role_payload_json[i].id==idJoueur){
            localStorage.setItem("pseudo",JSON.stringify(role_payload_json[i]))
        }
    }
    }

    if(role.status==500){
        alert("ca n a pas marche ")
    }   
}


async function start(){
    const data=localStorage.getItem("pseudo")
    const id_partie=JSON.parse(data).idPartie;
    const role = JSON.parse(data).role
    const players= await fetch("http://localhost:8080/players/"+id_partie)
    console.log(await players.json())
    if(role=="MAITRE_MOT"){
        window.location.href= "/frontend/wordsMaster.html"
    }
    else{
        window.location.href= "/frontend/intuitionMaster.html"
    }
    
}