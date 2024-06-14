export class RoleView
{
    constructor()
    {
    }

    updateRole(players)
    {
        const label_intuition = document.getElementById("MAITRE_INTUITION");
        const label_maitre_mot = document.getElementById("MAITRE_MOT");
        
        label_intuition.innerHTML = "";
        label_maitre_mot.innerHTML = ""; 
    
        // Cherche le joueur avec le role maitre intuition
        const p1 = players.filter(e => {return e.role==="MAITRE_INTUITION" })[0];
        if(p1){
            if(p1.nom==JSON.parse(localStorage.getItem("current_player")).nom){
                label_intuition.innerHTML = "(you) "+p1.nom ?? "";
            }
            else{
                label_intuition.innerHTML = p1.nom ?? "";
            }
            
        }

        // Cherche le joueur avec le role maitre mots
        const p2 = players.filter(e => {return e.role==="MAITRE_MOT" })[0];
        if(p2){
            if(p2.nom==JSON.parse(localStorage.getItem("current_player")).nom){
            label_maitre_mot.innerHTML = "(you) "+p2.nom ?? "";
            }
            else{
                label_maitre_mot.innerHTML = p2.nom ?? "";
            }
        }
    }
}